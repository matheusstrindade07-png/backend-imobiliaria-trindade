package backend;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Collections;

import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

@Service
public class GoogleCalendarService {

    public Calendar conectar() throws Exception {

        String credenciais = System.getenv("GOOGLE_CREDENTIALS_BASE64");

        if (credenciais == null || credenciais.isBlank()) {
            throw new IllegalStateException(
                "GOOGLE_CREDENTIALS_BASE64 não configurada no Render."
            );
        }

        byte[] json = Base64.getDecoder().decode(credenciais);

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new ByteArrayInputStream(json))
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
        .setApplicationName("Imobiliaria Trindade")
        .build();
    }

    public String getCalendarId() {

        String id = System.getenv("GOOGLE_CALENDAR_ID");

        if (id == null || id.isBlank()) {
            throw new IllegalStateException(
                "GOOGLE_CALENDAR_ID não configurado no Render."
            );
        }

        return id;
    }

    public void testarConexao() throws Exception {

        Calendar calendario = conectar();

        calendario.calendarList()
                .get(getCalendarId())
                .execute();

         System.out.println("Google Agenda conectado com sucesso!");
    }
}
