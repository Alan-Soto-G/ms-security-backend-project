package fbc.ms_security.Repositories;

import fbc.ms_security.Models.Entities.Permission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PermissionRepository extends MongoRepository<Permission, String> {

    @Query("{'url': ?0, 'method': ?1}")
    Permission getPermission(String url, String method);

    @Query(value = "{}", sort = "{'usageCount': -1}")
    List<Permission> findAllOrderByUsageDesc();
}
