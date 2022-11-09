package main.model.repository;

import main.model.entity.Guild;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuildRepository extends JpaRepository<Guild, Long> {

}