/**
 * Copyright (C) 2016, Antony Holmes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of copyright holder nor the names of its contributors 
 *     may be used to endorse or promote products derived from this software 
 *     without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.columbia.rdf.matcalc.toolbox.dna;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jebtk.bioinformatics.DataSource;
import org.jebtk.bioinformatics.genomic.GenomeAssembly;
import org.jebtk.core.AppService;
import org.jebtk.core.collections.ArrayListCreator;
import org.jebtk.core.collections.DefaultTreeMap;
import org.jebtk.core.collections.UniqueArrayList;
import org.jebtk.core.tree.CheckTreeNode;
import org.jebtk.core.tree.TreeNode;
import org.jebtk.core.tree.TreeRootNode;
import org.jebtk.modern.tree.ModernCheckTree;
import org.jebtk.modern.tree.ModernCheckTreeMode;

/**
 * Service for extracting DNA from sequences.
 *
 * @author Antony Holmes Holmes
 */
public class DnaService implements Iterable<GenomeAssembly> {

  // public static final Path RES_DIR = PathUtils.getPath("res/modules/dna");

  private static class DnaServiceLoader {
    private static final DnaService INSTANCE = new DnaService();
  }

  public static DnaService getInstance() {
    return DnaServiceLoader.INSTANCE;
  }

  private List<GenomeAssembly> mAssemblies = new UniqueArrayList<GenomeAssembly>(10);

  public void add(GenomeAssembly assembly) {
    mAssemblies.add(assembly);
  }

  public ModernCheckTree<GenomeAssembly> createTree() throws IOException {
    return createTree(ModernCheckTreeMode.RADIO);
  }

  public ModernCheckTree<GenomeAssembly> createTree(ModernCheckTreeMode mode) throws IOException {
    ModernCheckTree<GenomeAssembly> tree = new ModernCheckTree<GenomeAssembly>(mode);

    // Organize by type

    Map<DataSource, List<GenomeAssembly>> sourceMap = DefaultTreeMap.create(new ArrayListCreator<GenomeAssembly>());

    for (GenomeAssembly a : mAssemblies) {
      sourceMap.get(a.getDataSource()).add(a);
    }

    TreeRootNode<GenomeAssembly> root = new TreeRootNode<GenomeAssembly>();

    for (DataSource source : sourceMap.keySet()) {
      TreeNode<GenomeAssembly> node = new TreeNode<GenomeAssembly>(source.toString());

      for (GenomeAssembly a : sourceMap.get(source)) {
        for (String genome : a.getGenomes()) {
          CheckTreeNode<GenomeAssembly> child = new CheckTreeNode<GenomeAssembly>(genome, a);

          node.addChild(child);
        }
      }

      root.addChild(node);
    }

    tree.setRoot(root);

    tree.setChildrenAreExpanded(true, true);

    return tree;
  }

  @Override
  public Iterator<GenomeAssembly> iterator() {
    return mAssemblies.iterator();
  }
}