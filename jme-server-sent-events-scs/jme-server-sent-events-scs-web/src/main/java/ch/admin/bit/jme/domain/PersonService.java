package ch.admin.bit.jme.domain;

import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationService;
import ch.admin.bit.jeap.server.sent.events.domain.ResourceMutationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PersonService {

    private static final String RESOURCE_PATH = "persons";

    private final PersonRepository personRepository;
    private final ResourceMutationService resourceMutationService;

    public Person save(Person person) {
        Person savedPerson = personRepository.save(person);
        resourceMutationService.resourceMutation(ResourceMutationType.RESOURCE_CREATED, RESOURCE_PATH);
        log.info("Saved person {}", savedPerson);
        return savedPerson;
    }

    public Person update(Person person) {
        Person savedPerson = personRepository.save(person);
        resourceMutationService.resourceMutation(ResourceMutationType.RESOURCE_UPDATED, RESOURCE_PATH);
        log.info("Updated person {}", savedPerson);
        return savedPerson;
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Optional<Person> findById(UUID id) {
        return personRepository.findById(id);
    }

    public void deleteById(UUID id) {
        personRepository.deleteById(id);
        resourceMutationService.resourceMutation(ResourceMutationType.RESOURCE_DELETED, RESOURCE_PATH);
    }
}
