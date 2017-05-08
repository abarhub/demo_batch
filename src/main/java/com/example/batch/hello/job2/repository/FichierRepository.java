package com.example.batch.hello.job2.repository;

import com.example.batch.hello.job2.entity.Fichier;
import com.example.batch.hello.job2.entity.Operation;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Created by Alain on 07/05/2017.
 */
public interface FichierRepository extends CrudRepository<Fichier, Long> {

	Optional<Fichier> findByNomFichier(String nomFichier);
}
