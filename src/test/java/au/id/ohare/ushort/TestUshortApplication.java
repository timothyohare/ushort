package au.id.ohare.ushort;

import org.springframework.boot.SpringApplication;

public class TestUshortApplication {

	public static void main(String[] args) {
		SpringApplication.from(UshortApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
