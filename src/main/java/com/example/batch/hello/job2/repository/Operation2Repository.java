package com.example.batch.hello.job2.repository;

import com.example.batch.hello.job2.entity.Fichier;
import com.example.batch.hello.job2.entity.Operation;
import com.example.batch.hello.job2.entity.Operation2;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Created by Alain on 07/05/2017.
 */
public interface Operation2Repository extends CrudRepository<Operation2, Long> {

}
