package com.kokorono_note.kokorono_note;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // @Scheduledを付けた定期処理を有効にする
public class KokoronoNoteApplication {

	public static void main(String[] args) {
		SpringApplication.run(KokoronoNoteApplication.class, args);
	}

}
