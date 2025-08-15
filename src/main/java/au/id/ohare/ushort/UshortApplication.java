package au.id.ohare.ushort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class UshortApplication implements CommandLineRunner {

	private final Environment environment;

	public static void main(String[] args) {
		SpringApplication.run(UshortApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		String version = getClass().getPackage().getImplementationVersion();
		if (version == null) version = "development";
		
		String profile = String.join(",", environment.getActiveProfiles());
		if (profile.isEmpty()) profile = "default";
		
		log.info("Application started: version={}, profile={}", version, profile);
		log.info("URL Shortener application ready to serve requests");
	}
}
