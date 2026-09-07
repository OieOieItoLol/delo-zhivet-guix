package bot.dadata

import com.kuliginstepan.dadata.client.DadataClient
import com.kuliginstepan.dadata.client.domain.Suggestion
import com.kuliginstepan.dadata.client.domain.address.Address
import com.kuliginstepan.dadata.client.domain.address.AddressRequestBuilder
import org.springframework.stereotype.Service

@Service
class DadataService(private val dadataClient: DadataClient) : (String) -> Suggestion<Address> {

    override fun invoke(address: String): Suggestion<Address> {
        val addressRequest = AddressRequestBuilder.create(address).build()
        val addressSuggestion = dadataClient.suggestAddress(addressRequest).blockFirst()
        return addressSuggestion!!
    }
}
