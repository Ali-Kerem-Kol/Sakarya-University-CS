package com.project.project.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.project.entity.UserAccount;
import com.project.project.entity.Role;

/**
 * Data access layer for user accounts.
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    java.util.Optional<UserAccount> findByEmail(String email);

    java.util.Optional<UserAccount> findByEmailIgnoreCase(String email);

    java.util.Optional<UserAccount> findFirstByRoleOrderByIdAsc(Role role);

    long countByRole(com.project.project.entity.Role role);

    @Query("""
            select u.id from UserAccount u
            left join u.profile p
            where lower(u.email) like :query
               or lower(p.firstName) like :query
               or lower(p.lastName) like :query
            """)
    List<Long> findIdsBySearch(@Param("query") String query);

    @Query("select u.id from UserAccount u")
    List<Long> findAllIds();

    @EntityGraph(attributePaths = "profile")
    List<UserAccount> findByIdIn(List<Long> ids);

    @EntityGraph(attributePaths = "profile")
    @Query("select u from UserAccount u where u.id in :ids")
    List<UserAccount> findByIdInWithProfile(@Param("ids") List<Long> ids);

    @Query("""
            select distinct u.email
            from UserAccount u
            where u.role = :role
              and u.enabled = true
              and u.emailVerified = true
              and lower(u.email) like '%@ogr.sakarya.edu.tr'
            """)
    Set<String> findRecipientEmailsForAllStudents(@Param("role") Role role);
}
