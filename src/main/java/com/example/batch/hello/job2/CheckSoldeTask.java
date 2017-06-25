package com.example.batch.hello.job2;

import com.example.batch.hello.job2.entity.Fichier;
import com.example.batch.hello.job2.entity.Operation;
import com.example.batch.hello.job2.repository.FichierRepository;
import com.example.batch.hello.job2.repository.OperationRepository;
import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by Alain on 07/05/2017.
 */
public class CheckSoldeTask implements Tasklet {

	private static final Logger LOG = LoggerFactory.getLogger(CheckSoldeTask.class);

	private static final Splitter SPLITTER=Splitter.on(",")
			.omitEmptyStrings()
			.trimResults();

	@Value("${app.rep_comptes}")
	private String repertoireComptes;

	@Value("${app.comptes_ignorer:}")
	private String comptesAIgnorer;

	@Autowired
	private OperationRepository operationRepository;

	@Autowired
	private FichierRepository fichierRepository;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		LOG.info("checkSolde ...");

		Iterable<Fichier> iter = fichierRepository.findAll();

		Map<String, List<Fichier>> map = StreamSupport.stream(iter.spliterator(), false)
				.collect(groupingBy(Fichier::getNoCompte));

		for (Map.Entry<String, List<Fichier>> entry : map.entrySet()) {

			String noCompte=entry.getKey();
			List<Fichier> liste = entry.getValue();

			if(compteAIgnorer(noCompte)||true){
				LOG.info("ignore le compte {} ...", noCompte);
				continue;
			} else {
				LOG.info("traitement du compte {} ...", noCompte);
			}

			liste.forEach(f -> verifieFichier(f));
		}

		LOG.info("checkSolde ok");
		return RepeatStatus.FINISHED;
	}

	private boolean compteAIgnorer(String noCompte) {
		if(noCompte==null||noCompte.trim().isEmpty()){
			return true;
		} else if(comptesAIgnorer!=null&&!comptesAIgnorer.trim().isEmpty()){
			List<String> liste=getListeCompteAIgnorer();
			if(liste!=null){
				return liste.contains(noCompte);
			}
		}

		return false;
	}

	private List<String> getListeCompteAIgnorer(){
		return SPLITTER.splitToList(comptesAIgnorer);
	}

	private void verifieFichier(Fichier f) {
		if (f.isSoldeInitial()) {
			LOG.info("Fichier du solde initial ignore : " + f.getNomFichier());
		} else {
			LOG.info("Traitement fichier {}", f.getNomFichier());
			double soldeFinal = f.getSolde();

			Optional<Double> optTotal = operationRepository.totalOperations(f);

			double total=0;

			if(optTotal.isPresent()){
				total=optTotal.get();
			} else {
				LOG.info("Fichier {} ignore ?", f.getNomFichier());
				//return;
			}

			double total2 = 0.0;
			LocalDate d = null;
			for (Operation o : f.getListeOperations()) {
				total2 += o.getMontant();
				if (d == null) {
					d = o.getDate();
				} else if (d.isAfter(o.getDate())) {
					d = o.getDate();
				}
			}

			if (egal(total, total2)) {
				LOG.info("le montant total est correcte : {}", total);
			} else {
				LOG.error("le montant total est faux : {}!={}", total, total2);
				throw new IllegalStateException("le montant total est faux:" + total + "!=" + total2);
			}

			if (d == null) {
				throw new IllegalStateException("Impossible de trouver la date de debut");
			} else {
				LOG.info("1ere operation : {}", d);
			}

			double totalAvantFichier = operationRepository.totalOperationConsolide(d, f.getNoCompte());

			LOG.info("totalAvantFichier={}", totalAvantFichier);
			LOG.info("soldeFinal={}", soldeFinal);
			LOG.info("totalAvantFichier+total={}", totalAvantFichier + total);

			if (egal(soldeFinal, totalAvantFichier + total)) {
				LOG.info("Solde OK");
			} else {
				LOG.error("Erreur pour le calcul du solde : {} != {}", soldeFinal, totalAvantFichier + total);
				throw new IllegalStateException("Erreur pour le calcul du solde : " + soldeFinal + " != " + (totalAvantFichier + total));
			}

		}
	}

	private boolean egal(double m1, double m2) {
		return Math.abs(m1 - m2) < 0.001;
	}

}
