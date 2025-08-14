package au.id.ohare.ushort.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenUrlRequest {

    @NotBlank(message = "URL cannot be blank")
    @Size(max = 2048, message = "URL cannot exceed 2048 characters")
    private String url;
}