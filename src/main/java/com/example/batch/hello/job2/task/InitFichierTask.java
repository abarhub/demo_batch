package com.example.batch.hello.job2.task;

import com.example.batch.hello.job2.entity.Fichier;
import com.example.batch.hello.job2.repository.FichierRepository;
import com.example.batch.hello.job2.repository.OperationRepository;
import com.example.batch.hello.job2.service.FindRessources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Created by Alain on 07/05/2017.
 */
public class InitFichierTask implements Tasklet {

	private static final Logger LOG = LoggerFactory.getLogger(InitFichierTask.class);

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private static final DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd MMMM yyyy");

	//@Value("${app.rep_comptes}")
	//private String repertoireComptes;

	@Autowired
	private OperationRepository operationRepository;

	@Autowired
	private FichierRepository fichierRepository;

	@Autowired
	private FindRessources findRessources;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		LOG.info("init ...");
		List<Resource> listeRessources = findRessources.getListRessources();
		//ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
		//Resource[] resources = resolver.getResources(repertoireComptes + "/**/*.tsv");

		if (listeRessources != null && !listeRessources.isEmpty()) {
			for (Resource r : listeRessources) {
				boolean analyseOk = false;
				String nomFichier = r.getFilename();
				LOG.info("analyse du fichier {} ({})", nomFichier, r.getFile());
				Charset charset = calculCharSet(r);
				LOG.info("charset={}", charset);
				try (InputStream in = r.getInputStream()) {
					BufferedReader in2 = new BufferedReader(new InputStreamReader(in, charset));
					String ligne1 = in2.readLine();
					boolean isTraite;
					isTraite = traite(ligne1, in2, nomFichier, "Numéro Compte", "Type",
							"Compte tenu en", "Date", "Solde (EUROS)", formatter);
					if (isTraite) {
						analyseOk = true;
					} else {
						isTraite = traite(ligne1, in2, nomFichier, "Num Compte", "Libellé",
								"Compte tenu en", "Date", "Solde (EUROS)", formatter2);
						if (isTraite) {
							analyseOk = true;
						}
					}

					if (!analyseOk) {
						LOG.info("Erreur d'analyse");
					}
				}
			}
		}
		LOG.info("init ok");
		return RepeatStatus.FINISHED;
	}

	private boolean traite(String ligne1, BufferedReader in2, String nomFichier, String noCompte2, String type, String prefix2, String date1, String prefixSolde, DateTimeFormatter formatter) throws IOException {
		boolean analyseOk = false;
		if (ligne1 != null && ligne1.startsWith(noCompte2)) {
			String ligne2 = in2.readLine();
			if (ligne2 != null && ligne2.startsWith(type)) {
				String ligne3 = in2.readLine();
				if (ligne3 != null && ligne3.startsWith(prefix2)) {
					String ligne4 = in2.readLine();
					if (ligne4 != null && ligne4.startsWith(date1)) {
						String ligne5 = in2.readLine();
						if (ligne5 != null && ligne5.startsWith(prefixSolde)) {
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
									String s2=s.replace(',', '.');
									s2=s2.replace(" ","").trim();
									solde = Double.parseDouble(s2);
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
		return analyseOk;
	}

	private Charset calculCharSet(Resource r) {
		final Charset defaut = StandardCharsets.UTF_8;
		final String debut = "Numéro Compte";

		// test avec encoding utf-8
		try (InputStream in = r.getInputStream()) {
			Charset x = testEncoding(in, defaut);
			if (x != null) return x;

		} catch (IOException e) {
			return defaut;
		}

		// test avec encoding latin1
		try (InputStream in = r.getInputStream()) {
			Charset x = testEncoding(in, StandardCharsets.ISO_8859_1);
			if (x != null) return x;
//			BufferedReader in2 = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
//			String ligne = in2.readLine();
//			if (ligne != null && ligne.startsWith(debut)) {
//				return StandardCharsets.ISO_8859_1;
//			}
		} catch (IOException e) {
			return defaut;
		}
		return defaut;
	}

	private Charset testEncoding(InputStream in, Charset encoding) throws IOException {
		final String debut = "Numéro Compte";
		final String debut2 = "Libellé";
		BufferedReader in2 = new BufferedReader(new InputStreamReader(in, encoding));
		String ligne = in2.readLine();
		if (ligne == null) {
			return null;
		}
		if (ligne != null && ligne.startsWith(debut)) {
			return encoding;
		}
		ligne = in2.readLine();
		if (ligne != null && ligne.startsWith(debut2)) {
			return encoding;
		}
		return null;
	}
}
