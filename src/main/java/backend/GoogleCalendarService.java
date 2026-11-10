
package backend;

import backend.model.Agendamento;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;

@Service
public class GoogleCalendarService {

    private static final String CALENDAR_ID =
        "b9d56d7ed3aacbb04ea12d319d54ae83ea70435c06f5eb0eb080e51b061b8102@group.calendar.google.com";

    public Calendar conectar() throws Exception {

        GoogleCredentials credenciais =
            GoogleCredentials.fromStream(
                new FileInputStream(
                    "C:/Users/HP/Desktop/google-credentials/google-calendar-key.json"
                )
            ).createScoped(
                Collections.singleton(CalendarScopes.CALENDAR)
            );

        return new Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            new HttpCredentialsAdapter(credenciais)
        )
        .setApplicationName("Imobiliaria Trindade")
        .build();
    }

    public String criarEvento(Agendamento agendamento) throws Exception {

        Calendar calendario = conectar();

        ZoneId fuso = ZoneId.of("America/Sao_Paulo");

        LocalDateTime inicio = LocalDateTime.of(
            agendamento.getDataVisita(),
            agendamento.getHorario()
        );

        LocalDateTime fim = inicio.plusHours(1);

        Event evento = new Event()
            .setSummary("Visita - " + agendamento.getImovel())
            .setDescription(
                "Cliente: " + agendamento.getNome() +
                "\nTelefone: " + agendamento.getTelefone() +
                "\nE-mail: " + agendamento.getEmail()
            );

        evento.setStart(new EventDateTime()
            .setDateTime(new DateTime(
                inicio.atZone(fuso).toInstant().toEpochMilli()
            ))
            .setTimeZone("America/Sao_Paulo"));

        evento.setEnd(new EventDateTime()
            .setDateTime(new DateTime(
                fim.atZone(fuso).toInstant().toEpochMilli()
            ))
            .setTimeZone("America/Sao_Paulo"));

        Event criado = calendario.events()
            .insert(CALENDAR_ID, evento)
            .execute();

        return criado.getId();
    }

    public void testarConexao() throws Exception {

        Calendar calendario = conectar();

        calendario.calendars()
            .get(CALENDAR_ID)
            .execute();

        System.out.println("Google Agenda conectado com sucesso!");
    }
}
