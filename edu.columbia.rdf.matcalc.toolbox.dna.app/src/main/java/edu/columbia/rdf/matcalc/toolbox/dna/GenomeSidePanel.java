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
import org.jebtk.modern.button.CheckBox;
import org.jebtk.modern.button.ModernButtonGroup;
import org.jebtk.modern.button.ModernCheckSwitch;
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

  private Map<CheckBox, SequenceReader> mCheckMap = new HashMap<CheckBox, SequenceReader>();

  public GenomeSidePanel(String genome) {
    setHeader(
        new ModernSubHeadingLabel(Bio.ASSET_GENOMES).bottomBorder(10));

    IterMap<String, SequenceReader> assemblyMap = new IterTreeMap<String, SequenceReader>();

    /*
     * for (GenomeAssembly a : DnaService.getInstance()) { try { for (String
     * genome : a.getGenomes()) { assemblyMap.put(genome, a); } } catch
     * (IOException e) { e.printStackTrace(); } }
     */

    for (String g : SequenceReaderService.getInstance()) {
      assemblyMap.put(g, SequenceReaderService.getInstance().get(g));
    }

    Box box = VBox.create();

    //ModernButtonGroup bg = new ModernButtonGroup();

    //boolean first = true;

    // If two services provide the same genome, use the later.
    for (String g : assemblyMap) {
      CheckBox checkBox = new ModernCheckSwitch(g, g.equals(genome));
      mCheckMap.put(checkBox, assemblyMap.get(g));
      //bg.add(checkBox);
      box.add(checkBox);
    }

    setBody(new ModernScrollPane(box)
        .setHorizontalScrollBarPolicy(ScrollBarPolicy.NEVER));
    setBorder(DOUBLE_BORDER);
  }

  

  public List<SequenceReader> getsemblies() {
    List<SequenceReader> ret = new ArrayList<SequenceReader>(mCheckMap.size());

    for (ModernTwoStateWidget button : mCheckMap.keySet()) {
      if (button.isSelected()) {
        ret.add(mCheckMap.get(button));
      }
    }

    return ret;
  }

  public SequenceReader getsembly() {
    List<SequenceReader> assemblies = getsemblies();

    if (assemblies.size() > 0) {
      return assemblies.get(0);
    } else {
      return null;
    }
  }

  public List<String> getGenomes() {
    List<String> ret = new ArrayList<String>(mCheckMap.size());

    for (CheckBox button : mCheckMap.keySet()) {
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
