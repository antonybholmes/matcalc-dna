package edu.columbia.rdf.matcalc.toolbox.dna;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;

import org.jebtk.bioinformatics.genomic.GenomeAssembly;
import org.jebtk.core.collections.IterMap;
import org.jebtk.core.collections.IterTreeMap;
import org.jebtk.modern.ModernComponent;
import org.jebtk.modern.button.ModernButtonGroup;
import org.jebtk.modern.button.ModernRadioButton;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.scrollpane.ModernScrollPane;
import org.jebtk.modern.scrollpane.ScrollBarPolicy;
import org.jebtk.modern.text.ModernSubHeadingLabel;
import org.jebtk.modern.widget.ModernTwoStateWidget;

/**
 * Control which conservation scores are shown.
 * 
 * @author Antony Holmes Holmes
 *
 */
public class GenomeSidePanel extends ModernComponent {
	private static final long serialVersionUID = 1L;

	private Map<ModernRadioButton, GenomeAssembly> mCheckMap =
			new HashMap<ModernRadioButton, GenomeAssembly>();
	
	public GenomeSidePanel() {
		setHeader(new ModernSubHeadingLabel("Genome", DOUBLE_BOTTOM_BORDER)); 
		
		IterMap<String, GenomeAssembly> assemblyMap =
				new IterTreeMap<String, GenomeAssembly>();
		
		for (GenomeAssembly a : DnaService.getInstance()) {
			try {
				for (String genome : a.getGenomes()) {
					// Since items are loaded into the DNA service in order,
					// if two services serve hg19 for example, the latter will
					// be used to get the DNA.
					assemblyMap.put(genome, a);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Box box = VBox.create();
		
		ModernButtonGroup bg = new ModernButtonGroup();
		
		boolean first = true;
		
		// If two services provide the same genome, use the later.
		for (String genome : assemblyMap) {
			ModernRadioButton radio = new ModernRadioButton(genome, first);
			mCheckMap.put(radio, assemblyMap.get(genome));
			bg.add(radio);
			box.add(radio);
			first = false;
		}
		
		setBody(new ModernScrollPane(box)
				.setHorizontalScrollBarPolicy(ScrollBarPolicy.NEVER)
				.border(BORDER));
		setBorder(DOUBLE_BORDER);
	}

	public List<GenomeAssembly> getAssemblies() {
		List<GenomeAssembly> ret = new ArrayList<GenomeAssembly>(mCheckMap.size());

		for (ModernTwoStateWidget button : mCheckMap.keySet()) {
			if (button.isSelected()) {
				ret.add(mCheckMap.get(button));
			}
		}

		return ret;
	}

	public GenomeAssembly getAssembly() {
		List<GenomeAssembly> assemblies = getAssemblies();

		if (assemblies.size() > 0) {
			return assemblies.get(0);
		} else {
			return null;
		}
	}
	
	public List<String> getGenomes() {
		List<String> ret = new ArrayList<String>(mCheckMap.size());

		for (ModernRadioButton button : mCheckMap.keySet()) {
			if (button.isSelected()) {
				ret.add(button.getText());
			}
		}

		return ret;
	}

	/**
	 * Returns the first genome selected or null if there is no selection.
	 * 
	 * @return
	 */
	public String getGenome() {
		List<String> genomes = getGenomes();

		if (genomes.size() > 0) {
			return genomes.get(0);
		} else {
			return null;
		}
	}
}
