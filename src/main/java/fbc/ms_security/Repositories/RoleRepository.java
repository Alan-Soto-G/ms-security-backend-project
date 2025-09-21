package fbc.ms_security.Repositories;

import fbc.ms_security.Models.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {
    @Query("{'name': ?0}" )
    public Role getRoleByName(String name);
}
