package moe.pine.translatebot.translation;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.v3beta1.TranslationServiceClient;
import com.google.cloud.translate.v3beta1.TranslationServiceSettings;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class Translator {
    public Translator(final InputStream credentialsStream) {
        try {
            final GoogleCredentials credentials =
                    GoogleCredentials.fromStream(credentialsStream);

            //FixedCredentialsProvider.create()
//        TranslationServiceSettings.newBuilder()
            //              .setCredentialsProvider()
            //    TranslationServiceClient.create();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
