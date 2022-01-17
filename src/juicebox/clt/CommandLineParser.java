/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2021 Broad Institute, Aiden Lab, Rice University, Baylor College of Medicine
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
 *  FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package juicebox.clt;

import jargs.gnu.CmdLineParser;
import javastraw.reader.type.NormalizationHandler;
import javastraw.reader.type.NormalizationType;

import java.util.*;

/**
 * Command Line Parser for original (Pre/Dump) calls. Created by muhammadsaadshamim on 9/4/15.
 */
public class CommandLineParser extends CmdLineParser {

    // available
    // blou
    // used
    // d h x v n p k F V f t s g m q w c r z a y j i

    // universal
    protected final Option verboseOption = addBooleanOption('v', "verbose");
    protected final Option helpOption = addBooleanOption('h', "help");
    protected final Option versionOption = addBooleanOption('V', "version");

    // boolean
    private final Option allPearsonsOption = addBooleanOption('p', "pearsons-all-resolutions");
    private final Option noFragNormOption = addBooleanOption('F', "no_fragment-normalization");
    private final Option useMinRAM = addBooleanOption("conserve-ram");
    private final Option checkMemory = addBooleanOption("check-ram-usage");
    protected final Option normalizationTypeOption = addStringOption('k', "normalization");
    private final Option genomeWideOption = addIntegerOption('w', "genomewide");
    private final Option nearDiagonalOption = addIntegerOption("diagonal-cutoff");
    private final Option threadNumOption = addIntegerOption('j', "threads");
    private final Option matrixThreadNumOption = addIntegerOption("mthreads");
    private final Option resolutionOption = addStringOption('r', "resolutions");


    public CommandLineParser() {
    }


    /**
     * boolean flags
     */
    protected boolean optionToBoolean(Option option) {
        Object opt = getOptionValue(option);
        return opt != null && (Boolean) opt;
    }

    public boolean getHelpOption() { return optionToBoolean(helpOption);}

    public boolean getVerboseOption() {
        return optionToBoolean(verboseOption);
    }

    public boolean getAllPearsonsOption() {
        return optionToBoolean(allPearsonsOption);
    }

    public boolean getNoFragNormOption() {
        return optionToBoolean(noFragNormOption);
    }

    public boolean getVersionOption() {
        return optionToBoolean(versionOption);
    }

    public boolean getDontPutAllContactsIntoRAM() {
        return optionToBoolean(useMinRAM);
    }

    public boolean shouldCheckRAMUsage() {
        return optionToBoolean(checkMemory);
    }

    /**
     * String flags
     */
    protected String optionToString(Option option) {
        Object opt = getOptionValue(option);
        return opt == null ? null : opt.toString();
    }



    /**
     * int flags
     */
    protected int optionToInt(Option option) {
        Object opt = getOptionValue(option);
        return opt == null ? 0 : ((Number) opt).intValue();
    }

    public int getNearDiagonalOption() {
        return optionToInt(nearDiagonalOption);
    }

    public int getGenomeWideOption() {
        return optionToInt(genomeWideOption);
    }

    protected long optionToLong(Option option) {
        Object opt = getOptionValue(option);
        return opt == null ? 0 : ((Number) opt).longValue();
    }


    public enum Alignment {INNER, OUTER, LL, RR, TANDEM}

    public int getNumThreads() {
        return optionToInt(threadNumOption);
    }

    public int getNumMatrixOperationThreads() {
        return optionToInt(matrixThreadNumOption);
    }


    /**
     * double flags
     */
    protected double optionToDouble(Option option) {
        Object opt = getOptionValue(option);
        return opt == null ? -1 : ((Number) opt).doubleValue();
    }


    protected List<String> optionToStringList(Option option) {
        Object opt = getOptionValue(option);
        return opt == null ? null : new ArrayList<>(Arrays.asList(opt.toString().split(",")));
    }

    public List<String> getResolutionOption() {
        return optionToStringList(resolutionOption);
    }

    public List<NormalizationType> getAllNormalizationTypesOption() {
        NormalizationHandler normalizationHandler = new NormalizationHandler();
        List<String> normStrings = optionToStringList(normalizationTypeOption);
        if (normStrings == null || normStrings.size() < 1) {
            return normalizationHandler.getDefaultSetForHiCFileBuilding();
        }

        List<NormalizationType> normalizationTypes = new ArrayList<>();
        for (String normString : normStrings) {
            normalizationTypes.add(retrieveNormalization(normString, normalizationHandler));
        }

        return normalizationTypes;
    }

    protected NormalizationType retrieveNormalization(String norm, NormalizationHandler normalizationHandler) {
        if (norm == null || norm.length() < 1)
            return null;

        try {
            return normalizationHandler.getNormTypeFromString(norm);
        } catch (IllegalArgumentException error) {
            System.err.println("Normalization must be one of \"NONE\", \"VC\", \"VC_SQRT\", \"KR\", \"GW_KR\", \"GW_VC\", \"INTER_KR\", or \"INTER_VC\".");
            System.exit(7);
        }
        return null;
    }

}
