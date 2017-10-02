package edu.columbia.rdf.matcalc.toolbox.dna;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jebtk.bioinformatics.genomic.GenomeAssembly;
import org.jebtk.core.tree.CheckTreeNode;
import org.jebtk.modern.ModernComponent;
import org.jebtk.modern.scrollpane.ModernScrollPane;
import org.jebtk.modern.scrollpane.ScrollBarPolicy;
import org.jebtk.modern.tree.ModernCheckTree;
import org.jebtk.modern.tree.ModernCheckTreeMode;

/**
 * Control which conservation scores are shown.
 * 
 * @author Antony Holmes Holmes
 *
 */
public class DnaTreePanel extends ModernComponent {
	private static final long serialVersionUID = 1L;

	private ModernCheckTree<GenomeAssembly> mTree;

	private boolean mCheckAll = true;

	public DnaTreePanel() {
		this(ModernCheckTreeMode.RADIO);
	}

	public DnaTreePanel(ModernCheckTreeMode mode) {

		try {
			mTree = DnaService.getInstance().createTree(mode);

			ModernScrollPane scrollPane = new ModernScrollPane(mTree)
					.setHorizontalScrollBarPolicy(ScrollBarPolicy.NEVER);

			setBody(scrollPane); //new ModernContentPanel(scrollPane));
			
			// Set a default
			//((CheckTreeNode<String>)mTree.getChildByPath("/ucsc/hg19/ucsc_refseq_hg19")).setChecked(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<GenomeAssembly> getAssemblies() {
		List<CheckTreeNode<GenomeAssembly>> nodes = mTree.getCheckedNodes();

		List<GenomeAssembly> ret = new ArrayList<GenomeAssembly>(nodes.size());

		for (CheckTreeNode<GenomeAssembly> node : nodes) {
			GenomeAssembly v = node.getValue();
			
			if (v != null) {
				ret.add(node.getValue());
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
		List<CheckTreeNode<GenomeAssembly>> nodes = mTree.getCheckedNodes();

		List<String> ret = new ArrayList<String>(nodes.size());

		for (CheckTreeNode<GenomeAssembly> node : nodes) {
			ret.add(node.getName());
		}

		return ret;
	}

	public String getGenome() {
		List<String> genomes = getGenomes();

		if (genomes.size() > 0) {
			return genomes.get(0);
		} else {
			return null;
		}
	}
}
