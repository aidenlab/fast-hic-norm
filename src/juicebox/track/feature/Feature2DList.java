/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2015 Broad Institute, Aiden Lab
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package juicebox.track.feature;

import juicebox.data.HiCFileTools;
import juicebox.tools.utils.juicer.hiccups.HiCCUPSUtils;
import org.broad.igv.feature.Chromosome;

import java.awt.*;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

/**
 * List of two-dimensional features.  Hashtable for each chromosome for quick viewing.
 * Visibility depends on user selection.
 *
 * @author Neva Durand, Muhammad Shamim, Marie Hoeger
 */
public class Feature2DList {

    /**
     * List of 2D features stored by chromosome
     */
    private final Map<String, List<Feature2D>> featureList = new HashMap<String, List<Feature2D>>();

    /**
     * Visibility as set by user
     */
    private boolean isVisible;

    /**
     * Initialized hashtable
     */
    public Feature2DList() {
        isVisible = true;
    }

    /**
     * Helper method to get the key, lowest ordinal chromosome first
     *
     * @param chr1Idx First chromosome index
     * @param chr2Idx Second chromosome index
     * @return key
     */
    public static String getKey(int chr1Idx, int chr2Idx) {

        int c1;
        int c2;
        if (chr1Idx < chr2Idx) {
            c1 = chr1Idx;
            c2 = chr2Idx;
        } else {
            c1 = chr2Idx;
            c2 = chr1Idx;
        }

        return "" + c1 + "_" + c2;
    }

    /**
     * Helper method to get the key given chromosomes
     *
     * @param chr1 First chromosome
     * @param chr2 Second chromosome
     * @return key
     */
    public static String getKey(Chromosome chr1, Chromosome chr2) {
        return getKey(chr1.getIndex(), chr2.getIndex());
    }

    /**
     * Returns list of features on this chromosome pair
     *
     * @param chr1Idx First chromosome index
     * @param chr2Idx Second chromosome index
     * @return List of 2D features at that point
     */
    public List<Feature2D> get(int chr1Idx, int chr2Idx) {
        String key = getKey(chr1Idx, chr2Idx);
        if (!featureList.containsKey(key)) {
            List<Feature2D> features = new ArrayList<Feature2D>();
            featureList.put(key, features);
        }
        return featureList.get(key);
    }

    /**
     * Adds feature to appropriate chromosome pair list; key stored so that first chromosome always less than second
     *
     * @param chr1Idx First chromosome index
     * @param chr2Idx Second chromosome index
     * @param feature feature to add
     */
    public void add(int chr1Idx, int chr2Idx, Feature2D feature) {

        String key = getKey(chr1Idx, chr2Idx);
        addByKey(key, feature);

    }

    void addByKey(String key, Feature2D feature) {
        if (featureList.containsKey(key)) {
            featureList.get(key).add(feature);
        } else {
            List<Feature2D> loops = new ArrayList<Feature2D>();
            loops.add(feature);
            featureList.put(key, loops);
        }
    }

    /**
     * Returns visibility of list
     *
     * @return If list is visible
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Set visibility of list
     *
     * @param flag Visibility
     */
    public void setVisible(boolean flag) {
        isVisible = flag;
    }

    /**
     * Export feature list to given file path
     *
     * @param outputFilePath
     */
    public int exportFeatureList(String outputFilePath, boolean useOldHiccupsFormat) {
        if (featureList != null && featureList.size() > 0) {
            final PrintWriter outputFile = HiCFileTools.openWriter(outputFilePath);
            return exportFeatureList(outputFile, useOldHiccupsFormat);
        }
        return -1;
    }

    /**
     * Export feature list to given file path
     *
     * @param outputFile
     */
    public int exportFeatureList(final PrintWriter outputFile, final boolean useOldHiccupsFormat) {
        if (featureList != null && featureList.size() > 0) {

            Feature2D featureZero = extractSingleFeature();
            if (featureZero != null) {
                outputFile.println(featureZero.getOutputFileHeader());
                if (useOldHiccupsFormat) {
                    processLists(new FeatureFunction() {
                        @Override
                        public void process(String chr, List<Feature2D> feature2DList) {
                            for (Feature2D feature : feature2DList) {
                                outputFile.println(HiCCUPSUtils.oldOutput(feature));
                            }
                        }
                    });

                } else {
                    processLists(new FeatureFunction() {
                        @Override
                        public void process(String chr, List<Feature2D> feature2DList) {
                            for (Feature2D feature : feature2DList) {
                                outputFile.println(feature);
                            }
                        }
                    });
                }
            }
            outputFile.close();

            return 0;
        }
        return -1;
    }

    /**
     * Export feature list to given file path
     *
     * @param outputFile
     */
    public void autoSaveNew(PrintWriter outputFile, Feature2D feature) {
        if (featureList != null && featureList.size() > 0) {
            outputFile.println(feature);
        }
    }

    /**
     * Export feature list to given file path
     *
     * @param outputFile
     */
    public void autoSaveAll(PrintWriter outputFile) {
        if (featureList != null && featureList.size() > 0) {
            for (String key : featureList.keySet()) {
                for (Feature2D feature : featureList.get(key)) {
                    outputFile.println(feature);
                }
            }
        }
    }

    /**
     * Get first feature found
     *
     * @return feature
     */
    public Feature2D extractSingleFeature() {
        for (List<Feature2D> features : featureList.values()) {
            System.out.println("List1 Size " + features.size());
            for (Feature2D feature : features) {
                return feature;
            }
        }
        return null;
        // TODO this should not give no such element exceptions
        // TODO meh - custom annotation must be adding unnecessary keys?
        //return featureList.get(featureList.keySet().iterator().next()).iterator().next();
    }

    /*
     * Set color for the features
     * @param color
     */
    public void setColor(final Color color) {
        processLists(new FeatureFunction() {
            @Override
            public void process(String chr, List<Feature2D> feature2DList) {
                for (Feature2D feature : feature2DList) {
                    feature.setColor(color);
                }
            }
        });
    }

    /**
     * Adds features to appropriate chromosome pair list;
     * key stored so that first chromosome always less than second
     *
     * @param inputList
     * @return
     */
    public void add(Feature2DList inputList) {

        Set<String> inputKeySet = inputList.getKeySet();

        for (String inputKey : inputKeySet) {
            List<Feature2D> inputFeatures = inputList.getFeatureList(inputKey);

            if (featureList.containsKey(inputKey)) {
                featureList.get(inputKey).addAll(inputFeatures);
            } else {
                List<Feature2D> features = new ArrayList<Feature2D>();
                features.addAll(inputFeatures);
                featureList.put(inputKey, features);
            }
        }
    }

    /**
     * Adds features to appropriate chromosome pair list if same
     * or similar point not already in list;
     * key stored so that first chromosome always less than second
     *
     * @param inputList
     * @return
     */
    public void addUnique(Feature2DList inputList) {

        Set<String> inputKeySet = inputList.getKeySet();

        for (String inputKey : inputKeySet) {
            List<Feature2D> inputFeatures = inputList.getFeatureList(inputKey);

            if (featureList.containsKey(inputKey)) {
                //features.addAll(inputFeatures);
                addAllUnique(inputFeatures, featureList.get(inputKey));
            } else {
                List<Feature2D> features = new ArrayList<Feature2D>();
                features.addAll(inputFeatures);
                featureList.put(inputKey, features);
            }
        }
    }

    public Feature2DList getOverlap(Feature2DList inputList) {
        Feature2DList output = new Feature2DList();
        Set<String> inputKeySet = inputList.getKeySet();
        for (String inputKey : inputKeySet) {
            List<Feature2D> inputFeatures = inputList.getFeatureList(inputKey);
            // there are features in both lists

            if (featureList.containsKey(inputKey)) {
                for (Feature2D myFeature : featureList.get(inputKey)) {
                    if (doesOverlap(myFeature, inputFeatures)) {
                        output.addByKey(inputKey, myFeature);
                    }
                }
            }
        }
        return output;
    }

    // Compares a feature against all other features in list
    private boolean doesOverlap(Feature2D feature, List<Feature2D> existingFeatures) {
        boolean repeat = false;
        for (Feature2D existingFeature : existingFeatures) {
            if (existingFeature.overlapsWith(feature)) {
                repeat = true;
            }
        }
        return repeat;
    }

    // Iterate through new features and see if there is any overlap
    // TODO: implement this more efficiently, maybe rtree
    private void addAllUnique(List<Feature2D> inputFeatures, List<Feature2D> existingFeatures) {
        for (Feature2D inputFeature : inputFeatures) {
            // Compare input with existing points
            if (!doesOverlap(inputFeature, existingFeatures)) {
                existingFeatures.add(inputFeature);
            }
        }
    }

    public void addAttributeFieldToAll(final String newAttributeName, final String newAttributeValue) {
        processLists(new FeatureFunction() {
            @Override
            public void process(String chr, List<Feature2D> feature2DList) {
                for (Feature2D feature : feature2DList) {
                    if (!feature.containsAttributeKey(newAttributeName))
                        feature.addStringAttribute(newAttributeName, newAttributeValue);
                }
            }
        });
    }

    public void setAttributeFieldForAll(final String attributeName, final String attributeValue) {
        processLists(new FeatureFunction() {
            @Override
            public void process(String chr, List<Feature2D> feature2DList) {
                for (Feature2D feature : feature2DList) {
                    feature.setAttribute(attributeName, attributeValue);
                }
            }
        });
    }

    /**
     * Simple removal of exact duplicates (memory address)
     * TODO more detailed filtering by size/position/etc? NOTE that this is used by HiCCUPS
     */
    public void removeDuplicates() {
        filterLists(new FeatureFilter() {
            @Override
            public List<Feature2D> filter(String chr, List<Feature2D> feature2DList) {
                return new ArrayList<Feature2D>(new HashSet<Feature2D>(feature2DList));
            }
        });
    }


    /**
     * Get all keys (chromosome pairs) for hashmap
     *
     * @return keySet
     */
    Set<String> getKeySet() {
        return featureList.keySet();
    }

    /**
     * Get feature list corresponding to key (chromosome pair)
     *
     * @param key
     * @return
     */
    List<Feature2D> getFeatureList(String key) {
        return featureList.get(key);
    }

    /**
     * pass interface implementing a filter for features
     *
     * @param filter
     */
    public void filterLists(FeatureFilter filter) {
        for (String chr : featureList.keySet()) {
            featureList.put(chr, filter.filter(chr, featureList.get(chr)));
        }
    }

    /**
     * pass interface implementing a process for all features
     *
     * @param function
     */
    public void processLists(FeatureFunction function) {
        for (String chr : featureList.keySet()) {
            function.process(chr, featureList.get(chr));
        }
    }


    /**
     * @return true if features available for this region (key = "chr1_chr2")
     */
    boolean containsKey(String key) {
        return featureList.containsKey(key);
    }

    public int getNumTotalFeatures() {
        int total = 0;
        for (List<Feature2D> chrList : featureList.values()) {
            total += chrList.size();
        }
        return total;
    }
}
