package au.id.ohare.ushort.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenUrlResponse {

    private String originalUrl;
    private String shortenedUrl;
}