package com.example.batch.hello.job2.repository;

import com.example.batch.hello.job2.entity.Fichier;
import com.example.batch.hello.job2.entity.Operation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by Alain on 07/05/2017.
 */
public interface OperationRepository extends CrudRepository<Operation, Long> {

	@Query("select o from Operation o where o.date=?1 and o.libelle=?2 and abs(o.montant-?3)<0.001 and o.id<>?4 and o.fichier<>?5 and o.id<?6")
	List<Operation> findOperations(LocalDate date, String libelle, double montant, long id, Fichier fichier, Long oId);
}
