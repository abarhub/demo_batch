package com.example.batch.hello.job2.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alain on 25/06/2017.
 */
@Service
public class FindRessources {

	@Value("${app.rep_comptes}")
	private String repertoireComptes;

	@PostConstruct
	public void init() {
		if (repertoireComptes == null || repertoireComptes.trim().isEmpty()) {
			throw new IllegalArgumentException("Parametre 'app.rep_comptes' non renseigné");
		}
	}

	public List<Resource> getListRessources() throws IOException {
		List<Resource> res = new ArrayList<>();
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
		Resource[] resources = resolver.getResources(repertoireComptes + "/**/*.tsv");

		if (resources != null && resources.length > 0) {
			for (Resource r : resources) {
				res.add(r);
			}
		}

//		resources = resolver.getResources(repertoireComptes + "/**/*.csv");
//
//		if (resources != null && resources.length > 0) {
//			for (Resource r : resources) {
//				res.add(r);
//			}
//		}

		return res;
	}
}
