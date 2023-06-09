package main.model.repository;

import main.model.entity.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LockRepository extends JpaRepository<Lock, Long> {

    @Modifying
    @Transactional
    void deleteLockByUserId(long userId);
}