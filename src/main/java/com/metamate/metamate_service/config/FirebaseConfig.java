package com.metamate.metamate_service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() throws IOException {
        FileInputStream serviceAccount = new FileInputStream(
                Objects.requireNonNull(getClass().getClassLoader().getResource("firebase/serviceAccountKey.json")).getFile()
        );

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setStorageBucket("do-platform.firebasestorage.app") // Firebase 콘솔에서 확인한 버킷 이름
                .build();

        FirebaseApp.initializeApp(options);
    }
}
