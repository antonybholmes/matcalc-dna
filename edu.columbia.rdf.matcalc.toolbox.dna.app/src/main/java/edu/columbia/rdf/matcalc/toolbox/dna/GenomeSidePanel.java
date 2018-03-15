package edu.columbia.rdf.matcalc.toolbox.dna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;

import org.jebtk.bioinformatics.Bio;
import org.jebtk.bioinformatics.genomic.SequenceReader;
import org.jebtk.bioinformatics.genomic.SequenceReaderService;
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

  private Map<ModernRadioButton, SequenceReader> mCheckMap = new HashMap<ModernRadioButton, SequenceReader>();

  public GenomeSidePanel() {
    setHeader(
        new ModernSubHeadingLabel(Bio.ASSET_GENOME, DOUBLE_BOTTOM_BORDER));

    IterMap<String, SequenceReader> assemblyMap = new IterTreeMap<String, SequenceReader>();

    /*
     * for (GenomeAssembly a : DnaService.getInstance()) { try { for (String
     * genome : a.getGenomes()) { assemblyMap.put(genome, a); } } catch
     * (IOException e) { e.printStackTrace(); } }
     */

    for (String genome : SequenceReaderService.instance()) {
      assemblyMap.put(genome, SequenceReaderService.instance().get(genome));
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
        .setHorizontalScrollBarPolicy(ScrollBarPolicy.NEVER).border(BORDER));
    setBorder(DOUBLE_BORDER);
  }

  public List<SequenceReader> getAssemblies() {
    List<SequenceReader> ret = new ArrayList<SequenceReader>(mCheckMap.size());

    for (ModernTwoStateWidget button : mCheckMap.keySet()) {
      if (button.isSelected()) {
        ret.add(mCheckMap.get(button));
      }
    }

    return ret;
  }

  public SequenceReader getAssembly() {
    List<SequenceReader> assemblies = getAssemblies();

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
