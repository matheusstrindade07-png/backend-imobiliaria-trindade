
package backend;

import backend.model.Agendamento;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
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

    public String criarEvento(Agendamento agendamento) throws Exception {

        Calendar calendario = conectar();

        LocalDateTime inicio = LocalDateTime.of(
                agendamento.getDataVisita(),
                agendamento.getHorario()
        );

        LocalDateTime fim = inicio.plusHours(1);

        ZoneId zona = ZoneId.of("America/Sao_Paulo");

        Event evento = new Event()
                .setSummary("Visita ao imóvel - " + agendamento.getImovel())
                .setDescription(
                        "Cliente: " + agendamento.getNome() + "\n" +
                        "Telefone: " + agendamento.getTelefone() + "\n" +
                        "E-mail: " + agendamento.getEmail() + "\n" +
                        "Imóvel: " + agendamento.getImovel()
                )
                .setStart(new EventDateTime()
                        .setDateTime(new com.google.api.client.util.DateTime(
                                Date.from(inicio.atZone(zona).toInstant())
                        ))
                        .setTimeZone(zona.getId()))
                .setEnd(new EventDateTime()
                        .setDateTime(new com.google.api.client.util.DateTime(
                                Date.from(fim.atZone(zona).toInstant())
                        ))
                        .setTimeZone(zona.getId()));

        Event eventoCriado = calendario.events()
                .insert(getCalendarId(), evento)
                .execute();

        return eventoCriado.getId();
    }

    public void testarConexao() throws Exception {

        Calendar calendario = conectar();

        calendario.calendars()
                .get(getCalendarId())
                .execute();

        System.out.println("Google Agenda conectado com sucesso!");
    }
}
