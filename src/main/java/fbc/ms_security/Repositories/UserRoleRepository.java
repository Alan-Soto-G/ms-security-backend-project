package fbc.ms_security.Repositories;

import fbc.ms_security.Models.Relations.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends MongoRepository<UserRole, String> {
    @Query("{'user.$id': ObjectId(?0)}")
    public List<UserRole> getRolesByUser(String userId);

    @Query("{'role.$id': ObjectId(?0)}")
    public List<UserRole> getUsersByRole(String roleId);

    @Query("{'user.$id': ObjectId(?0), 'role.$id': ObjectId(?1)}")
    public Optional<UserRole> findByUserIdAndRoleId(String userId, String roleId);
}
