package project.spring_restful_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.spring_restful_api.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

}
