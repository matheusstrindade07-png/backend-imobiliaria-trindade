package backend;

import backend.model.Contato;
import backend.repository.ContatoRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class ContatoController {

    private final ContatoRepository repository;

    public ContatoController(ContatoRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/contato")
    public String receberContato(@RequestBody Contato contato) {

        repository.save(contato);

        System.out.println("Novo interesse recebido: " + contato.getNome());

        return "Interesse recebido com sucesso!";
    }
}