/*
 * Decompiled with CFR 0.152.
 */
package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.format;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.format.FeatureSet;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.format.FeatureSetBuilder;
import org.jetbrains.annotations.Nullable;

public enum MappingFormat {
    TINY_FILE("Tiny file", "tiny", true, FeatureSetBuilder.create().withNamespaces(true).withFileMetadata(FeatureSet.MetadataSupport.FIXED).withClasses(c2 -> c2.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.OPTIONAL).withRepackaging(true)).withFields(f -> f.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.OPTIONAL).withSrcDescs(FeatureSet.FeaturePresence.REQUIRED)).withMethods(m -> m.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.OPTIONAL).withSrcDescs(FeatureSet.FeaturePresence.REQUIRED)).withFileComments(true)),
    TINY_2_FILE("Tiny v2 file", "tiny", true, FeatureSetBuilder.create().withNamespaces(true).withFileMetadata(FeatureSet.MetadataSupport.ARBITRARY).withClasses(c2 -> c2.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.OPTIONAL).withRepackaging(true)).withFields(f -> f.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.OPTIONAL).withSrcDescs(FeatureSet.FeaturePresence.REQUIRED)).withMethods(m -> m.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.OPTIONAL).withSrcDescs(FeatureSet.FeaturePresence.REQUIRED)).withArgs(a -> a.withLvIndices(FeatureSet.FeaturePresence.REQUIRED).withSrcNames(FeatureSet.FeaturePresence.OPTIONAL).withDstNames(FeatureSet.FeaturePresence.OPTIONAL)).withVars(v -> v.withLvIndices(FeatureSet.FeaturePresence.REQUIRED).withLvtRowIndices(FeatureSet.FeaturePresence.OPTIONAL).withStartOpIndices(FeatureSet.FeaturePresence.REQUIRED).withSrcNames(FeatureSet.FeaturePresence.OPTIONAL).withDstNames(FeatureSet.FeaturePresence.OPTIONAL)).withElementComments(FeatureSet.ElementCommentSupport.SHARED).withFileComments(true)),
    ENIGMA_FILE("Enigma file", "mapping", true, FeatureSetBuilder.create().withElementMetadata(FeatureSet.MetadataSupport.FIXED).withClasses(c2 -> c2.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.OPTIONAL).withRepackaging(true)).withFields(f -> f.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.OPTIONAL).withSrcDescs(FeatureSet.FeaturePresence.REQUIRED)).withMethods(m -> m.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.OPTIONAL).withSrcDescs(FeatureSet.FeaturePresence.REQUIRED)).withArgs(a -> a.withLvIndices(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.OPTIONAL)).withElementComments(FeatureSet.ElementCommentSupport.SHARED).withFileComments(true)),
    ENIGMA_DIR("Enigma directory", null, true, FeatureSetBuilder.createFrom(MappingFormat.ENIGMA_FILE.features)),
    SRG_FILE("SRG file", "srg", true, FeatureSetBuilder.create().withPackages(p -> p.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED)).withClasses(c2 -> c2.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED).withRepackaging(true)).withFields(f -> f.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED)).withMethods(m -> m.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED).withSrcDescs(FeatureSet.FeaturePresence.REQUIRED).withDstDescs(FeatureSet.FeaturePresence.REQUIRED)).withFileComments(true)),
    XSRG_FILE("XSRG file", "xsrg", true, FeatureSetBuilder.createFrom(MappingFormat.SRG_FILE.features).withFields(f -> f.withSrcDescs(FeatureSet.FeaturePresence.REQUIRED).withDstDescs(FeatureSet.FeaturePresence.REQUIRED))),
    JAM_FILE("JAM file", "jam", true, FeatureSetBuilder.createFrom(MappingFormat.SRG_FILE.features).withPackages(p -> p.withSrcNames(FeatureSet.FeaturePresence.ABSENT).withDstNames(FeatureSet.FeaturePresence.ABSENT)).withFields(f -> f.withSrcDescs(FeatureSet.FeaturePresence.REQUIRED)).withMethods(m -> m.withDstDescs(FeatureSet.FeaturePresence.ABSENT)).withArgs(a -> a.withPositions(FeatureSet.FeaturePresence.REQUIRED).withSrcDescs(FeatureSet.FeaturePresence.OPTIONAL).withDstNames(FeatureSet.FeaturePresence.REQUIRED))),
    CSRG_FILE("CSRG file", "csrg", true, FeatureSetBuilder.createFrom(MappingFormat.SRG_FILE.features).withMethods(m -> m.withDstDescs(FeatureSet.FeaturePresence.ABSENT))),
    TSRG_FILE("TSRG file", "tsrg", true, FeatureSetBuilder.createFrom(MappingFormat.CSRG_FILE.features)),
    TSRG_2_FILE("TSRG v2 file", "tsrg", true, FeatureSetBuilder.createFrom(MappingFormat.TSRG_FILE.features).withNamespaces(true).withElementMetadata(FeatureSet.MetadataSupport.FIXED).withFields(f -> f.withSrcDescs(FeatureSet.FeaturePresence.OPTIONAL)).withArgs(a -> a.withLvIndices(FeatureSet.FeaturePresence.REQUIRED).withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED))),
    PROGUARD_FILE("ProGuard file", "txt", true, FeatureSetBuilder.create().withElementMetadata(FeatureSet.MetadataSupport.FIXED).withClasses(c2 -> c2.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED).withRepackaging(true)).withFields(f -> f.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED).withSrcDescs(FeatureSet.FeaturePresence.REQUIRED)).withMethods(m -> m.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED).withSrcDescs(FeatureSet.FeaturePresence.REQUIRED)).withFileComments(true)),
    INTELLIJ_MIGRATION_MAP_FILE("IntelliJ migration map file", "xml", true, FeatureSetBuilder.create().withFileMetadata(FeatureSet.MetadataSupport.FIXED).withPackages(p -> p.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED)).withClasses(c2 -> c2.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED).withRepackaging(true)).withFileComments(true)),
    RECAF_SIMPLE_FILE("Recaf Simple file", "txt", true, FeatureSetBuilder.create().withClasses(c2 -> c2.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED).withRepackaging(true)).withFields(f -> f.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withSrcDescs(FeatureSet.FeaturePresence.OPTIONAL).withDstNames(FeatureSet.FeaturePresence.REQUIRED)).withMethods(m -> m.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED).withSrcDescs(FeatureSet.FeaturePresence.REQUIRED)).withFileComments(true)),
    JOBF_FILE("JOBF file", "jobf", true, FeatureSetBuilder.create().withPackages(p -> p.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED)).withClasses(c2 -> c2.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED)).withFields(f -> f.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED).withSrcDescs(FeatureSet.FeaturePresence.REQUIRED)).withMethods(m -> m.withSrcNames(FeatureSet.FeaturePresence.REQUIRED).withDstNames(FeatureSet.FeaturePresence.REQUIRED).withSrcDescs(FeatureSet.FeaturePresence.REQUIRED)).withFileComments(true));

    private final FeatureSet features;
    public final String name;
    public final boolean hasWriter;
    @Nullable
    public final String fileExt;
    @Deprecated
    public final boolean hasNamespaces;
    @Deprecated
    public final boolean hasFieldDescriptors;
    @Deprecated
    public final boolean supportsComments;
    @Deprecated
    public final boolean supportsArgs;
    @Deprecated
    public final boolean supportsLocals;

    private MappingFormat(String name, String fileExt, boolean hasWriter, FeatureSetBuilder featureBuilder) {
        this.name = name;
        this.fileExt = fileExt;
        this.hasWriter = hasWriter;
        this.features = featureBuilder.build();
        this.hasNamespaces = this.features.hasNamespaces();
        this.hasFieldDescriptors = this.features.fields().srcDescs() != FeatureSet.FeaturePresence.ABSENT || this.features.fields().dstDescs() != FeatureSet.FeaturePresence.ABSENT;
        this.supportsComments = this.features.elementComments() != FeatureSet.ElementCommentSupport.NONE;
        this.supportsArgs = this.features.supportsArgs();
        this.supportsLocals = this.features.supportsVars();
    }

    public FeatureSet features() {
        return this.features;
    }

    public boolean hasSingleFile() {
        return this.fileExt != null;
    }

    public String getGlobPattern() {
        if (this.fileExt == null) {
            throw new UnsupportedOperationException("not applicable to dir based format");
        }
        return "*." + this.fileExt;
    }
}

