package fbc.ms_security.Repositories;

import fbc.ms_security.Models.Entities.Session;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends MongoRepository<Session, String> {

}
