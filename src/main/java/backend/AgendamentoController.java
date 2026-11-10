
package backend;

import backend.model.Agendamento;
import backend.repository.AgendamentoRepository;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agendamentos")
@CrossOrigin
public class AgendamentoController {

    private final AgendamentoRepository repository;
    private final GoogleCalendarService googleCalendar;

    public AgendamentoController(
            AgendamentoRepository repository,
            GoogleCalendarService googleCalendar) {
        this.repository = repository;
        this.googleCalendar = googleCalendar;
    }

    @PostMapping
    public Agendamento criarAgendamento(@RequestBody Agendamento agendamento) {
        agendamento.setStatus("PENDENTE");
        return repository.save(agendamento);
    }

    @GetMapping
    public List<Agendamento> listarAgendamentos() {
        return repository.findAll();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> dados) throws Exception {

        Agendamento agendamento = repository.findById(id).orElse(null);

        if (agendamento == null) {
            return ResponseEntity.notFound().build();
        }

        String status = dados.get("status");

        if (!"APROVADO".equals(status) && !"REJEITADO".equals(status)) {
            return ResponseEntity.badRequest().body("Status inválido.");
        }

        if ("APROVADO".equals(status)
                && agendamento.getGoogleEventId() == null) {

            String eventoId = googleCalendar.criarEvento(agendamento);

            agendamento.setGoogleEventId(eventoId);
        }

        agendamento.setStatus(status);

        return ResponseEntity.ok(repository.save(agendamento));
    }
}
