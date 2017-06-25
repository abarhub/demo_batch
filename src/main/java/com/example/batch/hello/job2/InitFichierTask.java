package com.example.batch.hello.job2;

import com.example.batch.hello.job2.entity.Fichier;
import com.example.batch.hello.job2.repository.FichierRepository;
import com.example.batch.hello.job2.repository.OperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Created by Alain on 07/05/2017.
 */
public class InitFichierTask implements Tasklet {

	private static final Logger LOG = LoggerFactory.getLogger(InitFichierTask.class);

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@Value("${app.rep_comptes}")
	private String repertoireComptes;

	@Autowired
	private OperationRepository operationRepository;

	@Autowired
	private FichierRepository fichierRepository;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		LOG.info("init ...");
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
		Resource[] resources = resolver.getResources(repertoireComptes + "/**/*.tsv");

		if (resources != null && resources.length > 0) {
			for (Resource r : resources) {
				boolean analyseOk = false;
				String nomFichier = r.getFilename();
				LOG.info("analyse du fichier {} ({})", nomFichier, r.getFile());
				Charset charset = calculCharSet(r);
				LOG.info("charset={}", charset);
				try (InputStream in = r.getInputStream()) {
					BufferedReader in2 = new BufferedReader(new InputStreamReader(in, charset));
					String ligne1 = in2.readLine();
					if (ligne1 != null && ligne1.startsWith("Numéro Compte")) {
						String ligne2 = in2.readLine();
						if (ligne2 != null && ligne2.startsWith("Type")) {
							String ligne3 = in2.readLine();
							if (ligne3 != null && ligne3.startsWith("Compte tenu en")) {
								String ligne4 = in2.readLine();
								if (ligne4 != null && ligne4.startsWith("Date")) {
									String ligne5 = in2.readLine();
									if (ligne5 != null && ligne5.startsWith("Solde (EUROS)")) {
										int pos;
										String noCompte = null, s;
										LocalDate date = null;
										double solde = 0.0;
										pos = ligne1.indexOf('\t');
										if (pos > 0) {
											noCompte = ligne1.substring(pos + 1);
											if (noCompte != null) {
												noCompte = noCompte.trim();
											}
										}
										pos = ligne4.indexOf('\t');
										if (pos > 0) {
											s = ligne4.substring(pos + 1);
											if (s != null) {
												s = s.trim();
											}
											if (!StringUtils.isEmpty(s)) {
												date = LocalDate.parse(s, formatter);
											}
										}
										pos = ligne5.indexOf('\t');
										if (pos > 0) {
											s = ligne5.substring(pos + 1);
											if (s != null) {
												s = s.trim();
											}
											if (!StringUtils.isEmpty(s)) {
												solde = Double.parseDouble(s.replace(',', '.'));
											}
										}
										if (!StringUtils.isEmpty(noCompte) && date != null && Math.abs(solde) > 0.001) {
											analyseOk = true;
										}
										LOG.info("noCompte={},date={},solde={},analyse={}", noCompte, date, solde, analyseOk);
										if (analyseOk) {
											Optional<Fichier> opt = fichierRepository.findByNomFichier(nomFichier);
											if (opt.isPresent()) {
												Fichier f = opt.get();
												f.setDate(date);
												f.setNoCompte(noCompte);
												f.setSolde(solde);
												fichierRepository.save(f);
											} else {
												Fichier f = new Fichier();
												f.setNomFichier(nomFichier);
												f.setDate(date);
												f.setNoCompte(noCompte);
												f.setSolde(solde);
												fichierRepository.save(f);
											}
										}
									}
								}
							}
						}
					}
					if (!analyseOk) {
						LOG.info("Erreur d'analyse");
					}
				}
			}
		}
		/*for (Fichier f : fichierRepository.findAll()) {
			LOG.info("traitement du fichier {} ...", f.getNomFichier());

			if (f.getListeOperations() != null && !f.getListeOperations().isEmpty()) {

				for (Operation o : f.getListeOperations()) {
					List<Operation> liste = operationRepository.findOperations(o.getDate(), o.getLibelle(), o.getMontant(), o.getId(),o.getFichier());
					if (liste != null && !liste.isEmpty()) {
						LOG.info("Presence multiple({})", liste.size());
					}

				}
			}

			LOG.info("traitement du fichier {} ok", f.getNomFichier());
		}*/
		LOG.info("init ok");
		return RepeatStatus.FINISHED;
	}

	private Charset calculCharSet(Resource r) {
		final Charset defaut = StandardCharsets.UTF_8;
		final String debut = "Numéro Compte";
		try (InputStream in = r.getInputStream()) {
			BufferedReader in2 = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			String ligne = in2.readLine();
			if (ligne == null) {
				return defaut;
			}
			if (ligne != null && ligne.startsWith(debut)) {
				return StandardCharsets.UTF_8;
			}
		} catch (IOException e) {
			return defaut;
		}
		try (InputStream in = r.getInputStream()) {
			BufferedReader in2 = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
			String ligne = in2.readLine();
			if (ligne != null && ligne.startsWith(debut)) {
				return StandardCharsets.ISO_8859_1;
			}
		} catch (IOException e) {
			return defaut;
		}
		return defaut;
	}
}
