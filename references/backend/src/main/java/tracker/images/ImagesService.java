package tracker.images;

import net.truej.sql.TrueSql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tracker.images.ImagesServiceG.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static tracker.TrackerApplication.*;

@Service @TrueSql
public class ImagesService {
    @Value("${images.rootLocation}")
    private Path rootLocation;
    @Value("${images.optimizedPhotoPrefix}")
    private String optimizedPhotoPrefix;

    @Autowired MainDb ds;

    public PhotoCredentials upload(
        int taskId, InputStream fileData, String fileName
    ) throws IOException {
        var photoId =
            ds.q("""
            select nextval('photo_id_sequence')::int as photo_id
            """).fetchOne(int.class);
        var recordDate = LocalDate.now(ZoneId.of("UTC"));
        var photoDirectoryPath = rootLocation
            .resolve(String.valueOf(recordDate.getYear()))
            .resolve(String.valueOf(recordDate.getMonthValue()))
            .resolve(String.valueOf(recordDate.getDayOfMonth()));

        if (!Files.exists(photoDirectoryPath))
            Files.createDirectories(photoDirectoryPath);

        var tempPhotoPath = photoDirectoryPath.resolve(
            UUID.randomUUID() + fileName
        );
        var convertedPhotoPath = photoDirectoryPath.resolve(photoId + ".jpg");
        var optimizedPhotoPath = photoDirectoryPath.resolve(optimizedPhotoPrefix + photoId + ".jpg");

        Files.copy(fileData, tempPhotoPath);

        var photoConvertCommand = new ProcessBuilder(
            "gm", "convert", "-strip", tempPhotoPath.toString(), convertedPhotoPath.toString()
        );
        var photoOptimizeCommand = new ProcessBuilder(
            "gm", "convert", "-size", "400x400", convertedPhotoPath.toString(),
            "-resize", "400x400", optimizedPhotoPath.toString()
        );

        var convertedPhotoRelativePath = rootLocation.relativize(convertedPhotoPath).toString();
        var optimizedPhotoRelativePath = rootLocation.relativize(optimizedPhotoPath).toString();

        try {
            photoConvertCommand.start().waitFor();
            photoOptimizeCommand.start().waitFor();

            ds.q("""
                insert into task_photo
                values (?, ?, ?, ?)
                """, taskId, photoId, convertedPhotoRelativePath, optimizedPhotoRelativePath
            ).fetchNone();
        } catch (Exception e) {
            Files.deleteIfExists(convertedPhotoPath);
            Files.deleteIfExists(optimizedPhotoPath);

            throw new RuntimeException("Ошибка при обработке файла: " + e);
        } finally {
            Files.deleteIfExists(tempPhotoPath);
        }
        return new PhotoCredentials(photoId, convertedPhotoRelativePath, optimizedPhotoRelativePath);
    }

    public void delete(int id) throws IOException {
        var photoDeletePaths =
            ds.q("""
                delete from task_photo
                where photo_id = ?
                returning photo_path, optimized_photo_path""", id
            ).g.fetchOneOrZero(PhotoDeletePaths.class);
        if (photoDeletePaths == null)
            return;

        Files.deleteIfExists(rootLocation.resolve(photoDeletePaths.photoPath));
        Files.deleteIfExists(rootLocation.resolve(photoDeletePaths.optimizedPhotoPath));
    }
}
