package tracker.api;

import net.truej.sql.TrueSql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tracker.images.PhotoCredentials;
import tracker.images.ImagesService;

import java.io.IOException;

@RequestMapping("/api/bot")
@TrueSql @RestController public class BotApi {

    @Autowired ImagesService imagesService;

    @PostMapping(value = "/photo/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    PhotoCredentials upload(
        @RequestParam("file") MultipartFile photo, @RequestParam int taskId
    ) throws IOException {
        try (var input = photo.getInputStream()) {
            return imagesService.upload(taskId, input, photo.getOriginalFilename());
        }
    }

    @PostMapping("/photo/delete") @ResponseBody
    void delete(@RequestBody int id) throws IOException {
        imagesService.delete(id);
    }
}
