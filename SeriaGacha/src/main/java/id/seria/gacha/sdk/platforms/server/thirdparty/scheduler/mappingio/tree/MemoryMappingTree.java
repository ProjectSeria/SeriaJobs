/*
 * Decompiled with CFR 0.152.
 */
package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.tree;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.MappedElementKind;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.MappingFlag;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.MappingVisitor;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.adapter.MappingSourceNsSwitch;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.tree.HierarchyInfoProvider;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.tree.MappingTree;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.tree.VisitOrder;
import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.tree.VisitableMappingTree;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class MemoryMappingTree
implements VisitableMappingTree {
    private boolean inVisitPass;
    private boolean indexByDstNames;
    private String srcNamespace;
    private List<String> dstNamespaces = Collections.emptyList();
    private final List<MappingTree.MetadataEntry> metadata = new ArrayList<MappingTree.MetadataEntry>();
    private final Map<String, ClassEntry> classesBySrcName = new LinkedHashMap<String, ClassEntry>();
    private final Collection<ClassEntry> classesView = Collections.unmodifiableCollection(this.classesBySrcName.values());
    private Map<String, ClassEntry>[] classesByDstNames;
    private HierarchyInfoProvider<?> hierarchyInfo;
    private int srcNsMap;
    private int[] dstNameMap;
    private Entry<?> currentEntry;
    private ClassEntry currentClass;
    private MethodEntry currentMethod;
    private Map<String, ClassEntry> pendingClasses;
    private Map<GlobalMemberKey, MemberEntry<?>> pendingMembers;

    public MemoryMappingTree() {
        this(false);
    }

    public MemoryMappingTree(boolean indexByDstNames) {
        this.indexByDstNames = indexByDstNames;
    }

    public MemoryMappingTree(MappingTree src) {
        if (src instanceof MemoryMappingTree) {
            this.indexByDstNames = ((MemoryMappingTree)src).indexByDstNames;
        }
        this.setSrcNamespace(src.getSrcNamespace());
        this.setDstNamespaces(src.getDstNamespaces());
        for (MappingTree.MetadataEntry metadataEntry : src.getMetadata()) {
            this.addMetadata(metadataEntry);
        }
        for (MappingTree.ClassMapping classMapping : src.getClasses()) {
            this.addClass(classMapping);
        }
    }

    public void setIndexByDstNames(boolean indexByDstNames) {
        this.assertNotInVisitPass();
        if (indexByDstNames == this.indexByDstNames) {
            return;
        }
        if (!indexByDstNames) {
            this.classesByDstNames = null;
        } else if (this.dstNamespaces != null) {
            this.initClassesByDstNames();
        }
        this.indexByDstNames = indexByDstNames;
    }

    private void initClassesByDstNames() {
        this.classesByDstNames = new Map[this.dstNamespaces.size()];
        for (int i = 0; i < this.classesByDstNames.length; ++i) {
            this.classesByDstNames[i] = new HashMap<String, ClassEntry>(this.classesBySrcName.size());
        }
        for (ClassEntry cls : this.classesBySrcName.values()) {
            for (int i = 0; i < cls.dstNames.length; ++i) {
                String dstName = cls.dstNames[i];
                if (dstName == null) continue;
                this.classesByDstNames[i].put(dstName, cls);
            }
        }
    }

    @ApiStatus.Experimental
    public void setHierarchyInfoProvider(@Nullable HierarchyInfoProvider<?> provider) {
        this.hierarchyInfo = provider;
        if (provider != null) {
            this.propagateNames(provider);
        }
    }

    @Override
    @Nullable
    public String getSrcNamespace() {
        return this.srcNamespace;
    }

    @Override
    @Nullable
    public String setSrcNamespace(String namespace) {
        this.assertNotInVisitPass();
        if (this.dstNamespaces.contains(namespace)) {
            throw new UnsupportedOperationException(String.format("Can't use name \"%s\" for the source namespace, as it's already in use by one of the destination namespaces %s. If a source namespace shuffle was the desired outcome, please resort to a %s instead; %s doesn't support this operation natively yet.", namespace, this.dstNamespaces, MappingSourceNsSwitch.class.getSimpleName(), this.getClass().getSimpleName()));
        }
        String ret = this.srcNamespace;
        this.srcNamespace = namespace;
        return ret;
    }

    @Override
    public List<String> getDstNamespaces() {
        return this.dstNamespaces;
    }

    @Override
    public List<String> setDstNamespaces(List<String> namespaces) {
        this.assertNotInVisitPass();
        if (!this.classesBySrcName.isEmpty()) {
            int newSize = namespaces.size();
            int[] nameMap = new int[newSize];
            HashSet<String> processedNamespaces = new HashSet<String>(newSize);
            HashSet<String> duplicateNamespaces = new HashSet<String>(newSize);
            for (int i = 0; i < newSize; ++i) {
                int oldNsIdx;
                String newNs = namespaces.get(i);
                if (newNs.equals(this.srcNamespace)) {
                    throw new UnsupportedOperationException(String.format("Can't use name \"%s\" for destination namespace %s, as it's already in use by the source namespace. If a source namespace shuffle was the desired outcome, please resort to a %s instead; %s doesn't support this operation natively yet.", newNs, i, MappingSourceNsSwitch.class.getSimpleName(), this.getClass().getSimpleName()));
                }
                nameMap[i] = oldNsIdx = this.dstNamespaces.indexOf(newNs);
                if (processedNamespaces.contains(newNs)) {
                    duplicateNamespaces.add(newNs);
                }
                processedNamespaces.add(newNs);
            }
            if (!duplicateNamespaces.isEmpty()) {
                throw new IllegalArgumentException("Duplicate destination namespace names: " + duplicateNamespaces);
            }
            boolean useResize = true;
            for (int i = 0; i < newSize; ++i) {
                int src = nameMap[i];
                if (src == i || src < 0 && i < this.dstNamespaces.size()) continue;
                useResize = false;
                break;
            }
            if (useResize) {
                this.resizeDstNames(newSize);
            } else {
                this.updateDstNames(nameMap);
            }
        }
        List<String> ret = this.dstNamespaces;
        this.dstNamespaces = namespaces;
        if (this.indexByDstNames) {
            this.initClassesByDstNames();
        }
        return ret;
    }

    private void resizeDstNames(int newSize) {
        for (ClassEntry cls : this.classesBySrcName.values()) {
            cls.resizeDstNames(newSize);
            for (FieldEntry field : cls.getFields()) {
                field.resizeDstNames(newSize);
            }
            for (MethodEntry method : cls.getMethods()) {
                method.resizeDstNames(newSize);
                for (MethodArgEntry arg : method.getArgs()) {
                    arg.resizeDstNames(newSize);
                }
                for (MethodVarEntry var : method.getVars()) {
                    var.resizeDstNames(newSize);
                }
            }
        }
    }

    private void updateDstNames(int[] nameMap) {
        for (ClassEntry cls : this.classesBySrcName.values()) {
            cls.updateDstNames(nameMap);
            for (FieldEntry field : cls.getFields()) {
                field.updateDstNames(nameMap);
            }
            for (MethodEntry method : cls.getMethods()) {
                method.updateDstNames(nameMap);
                for (MethodArgEntry arg : method.getArgs()) {
                    arg.updateDstNames(nameMap);
                }
                for (MethodVarEntry var : method.getVars()) {
                    var.updateDstNames(nameMap);
                }
            }
        }
    }

    @Override
    public List<? extends MappingTree.MetadataEntry> getMetadata() {
        return this.metadata;
    }

    @Override
    public List<? extends MappingTree.MetadataEntry> getMetadata(String key) {
        return Collections.unmodifiableList(this.metadata.stream().filter(entry -> entry.getKey().equals(key)).collect(Collectors.toList()));
    }

    @Override
    public void addMetadata(MappingTree.MetadataEntry entry) {
        this.metadata.add(entry);
    }

    @Override
    public boolean removeMetadata(String key) {
        return this.metadata.removeIf(entry -> entry.getKey().equals(key));
    }

    @Override
    public Collection<? extends MappingTree.ClassMapping> getClasses() {
        return this.classesView;
    }

    @Override
    @Nullable
    public MappingTree.ClassMapping getClass(String srcName) {
        return this.classesBySrcName.get(srcName);
    }

    @Override
    @Nullable
    public MappingTree.ClassMapping getClass(String name, int namespace) {
        if (namespace < 0 || !this.indexByDstNames) {
            return VisitableMappingTree.super.getClass(name, namespace);
        }
        return this.classesByDstNames[namespace].get(name);
    }

    @Override
    public MappingTree.ClassMapping addClass(MappingTree.ClassMapping cls) {
        this.assertNotInVisitPass();
        ClassEntry entry = cls instanceof ClassEntry && cls.getTree() == this ? (ClassEntry)cls : new ClassEntry(this, cls, this.getSrcNsEquivalent(cls));
        ClassEntry ret = this.classesBySrcName.putIfAbsent(cls.getSrcName(), entry);
        if (ret != null) {
            ret.copyFrom(entry, true);
            entry = ret;
        }
        if (this.indexByDstNames) {
            for (int i = 0; i < entry.dstNames.length; ++i) {
                String dstName = entry.dstNames[i];
                if (dstName == null) continue;
                this.classesByDstNames[i].put(dstName, entry);
            }
        }
        return entry;
    }

    private int getSrcNsEquivalent(MappingTree.ElementMapping mapping) {
        int ret = mapping.getTree().getNamespaceId(this.srcNamespace);
        if (ret == -2) {
            throw new UnsupportedOperationException("can't find source namespace in referenced mapping tree");
        }
        return ret;
    }

    @Override
    @Nullable
    public MappingTree.ClassMapping removeClass(String srcName) {
        this.assertNotInVisitPass();
        ClassEntry ret = this.classesBySrcName.remove(srcName);
        if (ret != null && this.indexByDstNames) {
            for (int i = 0; i < ret.dstNames.length; ++i) {
                String dstName = ret.dstNames[i];
                if (dstName == null) continue;
                this.classesByDstNames[i].remove(dstName);
            }
        }
        return ret;
    }

    @Override
    public void accept(MappingVisitor visitor, VisitOrder order) throws IOException {
        do {
            if (visitor.visitHeader()) {
                visitor.visitNamespaces(this.srcNamespace, this.dstNamespaces);
                Collection<MappingTree.MetadataEntry> metadataToVisit = this.metadata;
                if (visitor.getFlags().contains((Object)MappingFlag.NEEDS_METADATA_UNIQUENESS)) {
                    ArrayDeque<MappingTree.MetadataEntry> uniqueMetadata = new ArrayDeque<>();
                    HashSet<String> addedKeys = new HashSet<>();
                    for (int i = this.metadata.size() - 1; i >= 0; --i) {
                        MappingTree.MetadataEntry entry = this.metadata.get(i);
                        if (addedKeys.contains(entry.getKey())) continue;
                        addedKeys.add(entry.getKey());
                        uniqueMetadata.addFirst(entry);
                    }
                    metadataToVisit = uniqueMetadata;
                }
                for (MappingTree.MetadataEntry entry : metadataToVisit) {
                    visitor.visitMetadata(entry.getKey(), entry.getValue());
                }
            }
            if (!visitor.visitContent()) continue;
            Set<MappingFlag> flags = visitor.getFlags();
            boolean supplyFieldDstDescs = flags.contains((Object)MappingFlag.NEEDS_DST_FIELD_DESC);
            boolean supplyMethodDstDescs = flags.contains((Object)MappingFlag.NEEDS_DST_METHOD_DESC);
            for (ClassEntry cls : order.sortClasses(this.classesBySrcName.values())) {
                cls.accept(visitor, order, supplyFieldDstDescs, supplyMethodDstDescs);
            }
        } while (!visitor.visitEnd());
    }

    @Override
    public void reset() {
        this.inVisitPass = false;
        this.srcNsMap = -1;
        this.dstNameMap = null;
        this.currentEntry = null;
        this.currentClass = null;
        this.currentMethod = null;
        this.pendingClasses = null;
        this.pendingMembers = null;
    }

    @Override
    public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) {
        this.inVisitPass = true;
        this.srcNsMap = -1;
        this.dstNameMap = new int[dstNamespaces.size()];
        if (this.srcNamespace != null) {
            if (!srcNamespace.equals(this.srcNamespace)) {
                this.srcNsMap = this.dstNamespaces.indexOf(srcNamespace);
                if (this.srcNsMap < 0) {
                    this.reset();
                    throw new IllegalArgumentException("can't merge with disassociated src namespace");
                }
            }
            int newDstNamespaces = 0;
            for (int i = 0; i < this.dstNameMap.length; ++i) {
                int idx;
                String dstNs = dstNamespaces.get(i);
                if (dstNs.equals(this.srcNamespace)) {
                    idx = -1;
                } else {
                    if (dstNs.equals(srcNamespace)) {
                        this.reset();
                        throw new IllegalArgumentException("namespace \"" + srcNamespace + "\" is present on both source and destination side simultaneously");
                    }
                    idx = this.dstNamespaces.indexOf(dstNs);
                    if (idx < 0) {
                        if (newDstNamespaces == 0) {
                            this.dstNamespaces = new ArrayList<String>(this.dstNamespaces);
                        }
                        idx = this.dstNamespaces.size();
                        this.dstNamespaces.add(dstNs);
                        ++newDstNamespaces;
                    }
                }
                this.dstNameMap[i] = idx;
            }
            if (newDstNamespaces > 0) {
                int newSize = this.dstNamespaces.size();
                this.resizeDstNames(newSize);
                if (this.indexByDstNames) {
                    this.classesByDstNames = Arrays.copyOf(this.classesByDstNames, newSize);
                    for (int i = newSize - newDstNamespaces; i < this.classesByDstNames.length; ++i) {
                        this.classesByDstNames[i] = new HashMap<String, ClassEntry>(this.classesBySrcName.size());
                    }
                }
            }
        } else {
            this.srcNamespace = srcNamespace;
            this.dstNamespaces = dstNamespaces;
            for (int i = 0; i < this.dstNameMap.length; ++i) {
                if (dstNamespaces.get(i).equals(srcNamespace)) {
                    this.reset();
                    throw new IllegalArgumentException("namespace \"" + srcNamespace + "\" is present on both source and destination side simultaneously");
                }
                this.dstNameMap[i] = i;
            }
            if (this.indexByDstNames) {
                this.initClassesByDstNames();
            }
        }
    }

    @Override
    public void visitMetadata(String key, @Nullable String value) {
        MetadataEntryImpl entry = new MetadataEntryImpl(key, value);
        this.metadata.add(entry);
    }

    @Override
    public boolean visitClass(String srcName) {
        this.currentMethod = null;
        ClassEntry cls = (ClassEntry)this.getClass(srcName, this.srcNsMap);
        if (cls == null) {
            if (this.srcNsMap >= 0) {
                cls = this.queuePendingClass(srcName);
            } else {
                cls = new ClassEntry(this, srcName);
                this.classesBySrcName.put(srcName, cls);
            }
        }
        this.currentEntry = this.currentClass = cls;
        return true;
    }

    @Override
    public boolean visitField(String srcName, @Nullable String srcDesc) {
        if (this.currentClass == null) {
            throw new UnsupportedOperationException("Tried to visit field before owning class");
        }
        this.currentMethod = null;
        FieldEntry field = this.currentClass.getField(srcName, srcDesc, this.srcNsMap);
        if (field == null) {
            if (this.srcNsMap >= 0) {
                field = (FieldEntry)this.queuePendingMember(srcName, srcDesc, true);
            } else {
                field = new FieldEntry(this.currentClass, srcName, srcDesc);
                field = this.currentClass.addFieldInternal(field);
            }
        } else if (srcDesc != null && field.srcDesc == null) {
            if (this.srcNsMap >= 0) {
                this.queuePendingMember(srcName, srcDesc, true).setSrcName(field.getSrcName());
            } else {
                field.setSrcDescInternal(srcDesc);
            }
        }
        this.currentEntry = field;
        return true;
    }

    @Override
    public boolean visitMethod(String srcName, @Nullable String srcDesc) {
        if (this.currentClass == null) {
            throw new UnsupportedOperationException("Tried to visit method before owning class");
        }
        MethodEntry method = this.currentClass.getMethod(srcName, srcDesc, this.srcNsMap);
        if (method == null) {
            if (this.srcNsMap >= 0) {
                method = (MethodEntry)this.queuePendingMember(srcName, srcDesc, false);
            } else {
                method = new MethodEntry(this.currentClass, srcName, srcDesc);
                method = this.currentClass.addMethodInternal(method);
            }
        } else if (MemoryMappingTree.isValidDescriptor(srcDesc, true) && !MemoryMappingTree.isValidDescriptor(method.srcDesc, true)) {
            if (this.srcNsMap >= 0) {
                this.queuePendingMember(srcName, srcDesc, false).setSrcName(method.getSrcName());
            } else {
                method.setSrcDescInternal(srcDesc);
            }
        }
        this.currentEntry = this.currentMethod = method;
        return true;
    }

    private ClassEntry queuePendingClass(String name) {
        ClassEntry cls;
        if (this.pendingClasses == null) {
            this.pendingClasses = new HashMap<String, ClassEntry>();
        }
        if ((cls = this.pendingClasses.get(name)) == null) {
            cls = new ClassEntry(this, null);
            this.pendingClasses.put(name, cls);
        }
        assert (this.srcNsMap >= 0);
        cls.setDstNameInternal(name, this.srcNsMap);
        return cls;
    }

    private MemberEntry<?> queuePendingMember(String name, @Nullable String desc, boolean isField) {
        GlobalMemberKey key;
        MemberEntry member;
        if (this.pendingMembers == null) {
            this.pendingMembers = new HashMap();
        }
        if ((member = this.pendingMembers.get(key = new GlobalMemberKey(this.currentClass, name, desc, isField))) == null) {
            member = isField ? new FieldEntry(this.currentClass, null, desc) : new MethodEntry(this.currentClass, null, desc);
            this.pendingMembers.put(key, member);
        }
        assert (this.srcNsMap >= 0);
        member.setDstNameInternal(name, this.srcNsMap);
        return member;
    }

    private void addPendingClass(ClassEntry cls) {
        if (cls.isSrcNameMissing()) {
            return;
        }
        String srcName = cls.getSrcName();
        ClassEntry existing = this.classesBySrcName.get(srcName);
        if (existing == null) {
            this.classesBySrcName.put(srcName, cls);
        } else {
            existing.copyFrom(cls, true);
        }
    }

    private void addPendingMember(MemberEntry<?> member) {
        if (member.isSrcNameMissing() || member.getOwner().isSrcNameMissing()) {
            return;
        }
        ClassEntry owner = this.classesBySrcName.get(member.getOwner().getSrcName());
        member.setOwner(owner);
        boolean isField = member.getKind() == MappedElementKind.FIELD;
        String srcName = member.getSrcName();
        String dstDesc = member.getSrcDesc();
        String srcDesc = null;
        if (MemoryMappingTree.isValidDescriptor(dstDesc, !isField)) {
            srcDesc = this.mapDesc(dstDesc, this.srcNsMap, -1);
        }
        member.setSrcDescInternal(srcDesc);
        if (isField) {
            FieldEntry queuedField = (FieldEntry)member;
            FieldEntry existingField = owner.getField(srcName, srcDesc);
            if (existingField == null) {
                owner.addFieldInternal(queuedField);
            } else {
                existingField.copyFrom(queuedField, true);
            }
        } else {
            MethodEntry queuedMethod = (MethodEntry)member;
            MethodEntry existingMethod = owner.getMethod(srcName, srcDesc);
            if (existingMethod == null) {
                owner.addMethodInternal(queuedMethod);
            } else {
                existingMethod.copyFrom(queuedMethod, true);
            }
        }
    }

    @Override
    public boolean visitMethodArg(int argPosition, int lvIndex, @Nullable String srcName) {
        if (this.currentMethod == null) {
            throw new UnsupportedOperationException("Tried to visit method argument before owning method");
        }
        MethodArgEntry arg = this.currentMethod.getArg(argPosition, lvIndex, srcName);
        if (arg == null) {
            arg = new MethodArgEntry(this.currentMethod, argPosition, lvIndex, srcName);
            arg = this.currentMethod.addArgInternal(arg);
        } else {
            if (argPosition >= 0 && arg.argPosition < 0) {
                arg.setArgPositionInternal(argPosition);
            }
            if (lvIndex >= 0 && arg.lvIndex < 0) {
                arg.setLvIndexInternal(lvIndex);
            }
            if (srcName != null) {
                assert (!srcName.isEmpty());
                arg.setSrcName(srcName);
            }
        }
        this.currentEntry = arg;
        return true;
    }

    @Override
    public boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, @Nullable String srcName) {
        if (this.currentMethod == null) {
            throw new UnsupportedOperationException("Tried to visit method variable before owning method");
        }
        MethodVarEntry var = this.currentMethod.getVar(lvtRowIndex, lvIndex, startOpIdx, endOpIdx, srcName);
        if (var == null) {
            var = new MethodVarEntry(this.currentMethod, lvtRowIndex, lvIndex, startOpIdx, endOpIdx, srcName);
            var = this.currentMethod.addVarInternal(var);
        } else {
            if (lvtRowIndex >= 0 && var.lvtRowIndex < 0) {
                var.setLvtRowIndexInternal(lvtRowIndex);
            }
            if (lvIndex >= 0 && startOpIdx >= 0 && (var.lvIndex < 0 || var.startOpIdx < 0)) {
                var.setLvIndexInternal(lvIndex, startOpIdx, endOpIdx);
            }
            if (srcName != null) {
                assert (!srcName.isEmpty());
                var.setSrcName(srcName);
            }
        }
        this.currentEntry = var;
        return true;
    }

    @Override
    public boolean visitEnd() {
        if (this.pendingClasses != null) {
            for (ClassEntry classEntry : this.pendingClasses.values()) {
                this.addPendingClass(classEntry);
            }
            this.pendingClasses = null;
        }
        if (this.pendingMembers != null) {
            for (MemberEntry memberEntry : this.pendingMembers.values()) {
                this.addPendingMember(memberEntry);
            }
            this.pendingMembers = null;
        }
        this.reset();
        if (this.hierarchyInfo != null) {
            this.propagateNames(this.hierarchyInfo);
        }
        return true;
    }

    private <T> void propagateNames(HierarchyInfoProvider<T> provider) {
        int nsId = this.getNamespaceId(provider.getNamespace());
        if (nsId == -2) {
            return;
        }
        Set processed = Collections.newSetFromMap(new IdentityHashMap());
        for (ClassEntry cls : this.classesBySrcName.values()) {
            for (MethodEntry method : cls.getMethods()) {
                String curName;
                int i;
                Collection<? extends MappingTree.MethodMapping> hierarchyMethods;
                T hierarchy;
                String name = method.getName(nsId);
                if (name == null || name.startsWith("<") || !processed.add(method) || provider.getHierarchySize(hierarchy = provider.getMethodHierarchy(method)) <= 1 || (hierarchyMethods = provider.getHierarchyMethods(hierarchy, this)).size() <= 1) continue;
                String[] dstNames = new String[this.dstNamespaces.size()];
                int rem = dstNames.length;
                block2: for (MappingTree.MethodMapping m : hierarchyMethods) {
                    for (i = 0; i < dstNames.length; ++i) {
                        if (dstNames[i] != null || (curName = m.getDstName(i)) == null) continue;
                        dstNames[i] = curName;
                        if (--rem == 0) break block2;
                    }
                }
                for (MappingTree.MethodMapping m : hierarchyMethods) {
                    processed.add((MethodEntry)m);
                    for (i = 0; i < dstNames.length; ++i) {
                        curName = dstNames[i];
                        if (curName == null) continue;
                        m.setDstName(curName, i);
                    }
                }
            }
        }
    }

    @Override
    public void visitDstName(MappedElementKind targetKind, int namespace, String name) {
        namespace = this.dstNameMap[namespace];
        if (this.currentEntry == null) {
            throw new UnsupportedOperationException("Tried to visit mapped name before owner");
        }
        if (namespace < 0) {
            if (name.equals(this.currentEntry.getSrcNameUnchecked())) {
                return;
            }
            switch (this.currentEntry.getKind()) {
                case CLASS: {
                    assert (this.currentClass == this.currentEntry);
                }
                case FIELD: 
                case METHOD: {
                    if (!this.currentEntry.isSrcNameMissing()) break;
                    this.currentEntry.setSrcName(name);
                    return;
                }
                case METHOD_ARG: 
                case METHOD_VAR: {
                    this.currentEntry.setSrcName(name);
                    return;
                }
            }
            throw new UnsupportedOperationException("can't change src name for " + (Object)((Object)this.currentEntry.getKind()));
        }
        this.currentEntry.setDstNameInternal(name, namespace);
    }

    @Override
    public void visitComment(MappedElementKind targetKind, String comment) {
        Entry entry;
        switch (targetKind) {
            case CLASS: {
                entry = this.currentClass;
                break;
            }
            case METHOD: {
                entry = this.currentMethod;
                break;
            }
            default: {
                entry = this.currentEntry;
            }
        }
        if (entry == null) {
            throw new UnsupportedOperationException("Tried to visit comment before owning target");
        }
        entry.setCommentInternal(comment);
    }

    private static boolean isValidDescriptor(String descriptor, boolean possiblyMethod) {
        if (descriptor == null) {
            return false;
        }
        return !possiblyMethod || !descriptor.endsWith(")");
    }

    void assertNotInVisitPass() {
        if (this.inVisitPass) {
            throw new UnsupportedOperationException("Attempted illegal tree interaction via tree-API during an ongoing visitation pass");
        }
    }

    static final class ClassEntry
    extends Entry<ClassEntry>
    implements MappingTree.ClassMapping {
        private static final byte FLAG_HAS_ANY_FIELD_DESC = 1;
        private static final byte FLAG_MISSES_ANY_FIELD_DESC = 2;
        private static final byte FLAG_HAS_ANY_METHOD_DESC = 4;
        private static final byte FLAG_MISSES_ANY_METHOD_DESC = 8;
        private Map<MemberKey, FieldEntry> fields = null;
        private Map<MemberKey, MethodEntry> methods = null;
        private Collection<FieldEntry> fieldsView = null;
        private Collection<MethodEntry> methodsView = null;
        private byte flags;

        ClassEntry(MemoryMappingTree tree, String srcName) {
            super(tree, srcName);
        }

        ClassEntry(MemoryMappingTree tree, MappingTree.ClassMapping src, int srcNsEquivalent) {
            super(tree, src, srcNsEquivalent);
            for (MappingTree.FieldMapping fieldMapping : src.getFields()) {
                this.addFieldInternal(fieldMapping);
            }
            for (MappingTree.MethodMapping methodMapping : src.getMethods()) {
                this.addMethodInternal(methodMapping);
            }
        }

        @Override
        public MappedElementKind getKind() {
            return MappedElementKind.CLASS;
        }

        @Override
        public MemoryMappingTree getTree() {
            return this.tree;
        }

        @Override
        void setDstNameInternal(String name, int namespace) {
            String oldName;
            if (this.tree.indexByDstNames && !Objects.equals(name, oldName = this.dstNames[namespace])) {
                Map map = this.tree.classesByDstNames[namespace];
                if (oldName != null) {
                    map.remove(oldName);
                }
                if (name != null) {
                    map.put(name, this);
                } else {
                    map.remove(oldName);
                }
            }
            super.setDstNameInternal(name, namespace);
        }

        public Collection<FieldEntry> getFields() {
            if (this.fields == null) {
                return Collections.emptyList();
            }
            return this.fieldsView;
        }

        @Override
        @Nullable
        public FieldEntry getField(String srcName, @Nullable String srcDesc) {
            return ClassEntry.getMember(srcName, srcDesc, this.fields, this.flags, 1, 2);
        }

        @Override
        @Nullable
        public FieldEntry getField(String name, @Nullable String desc, int namespace) {
            return (FieldEntry)MappingTree.ClassMapping.super.getField(name, desc, namespace);
        }

        @Override
        public FieldEntry addField(MappingTree.FieldMapping field) {
            this.tree.assertNotInVisitPass();
            return this.addFieldInternal(field);
        }

        FieldEntry addFieldInternal(MappingTree.FieldMapping field) {
            FieldEntry entry;
            FieldEntry fieldEntry = entry = field instanceof FieldEntry && field.getOwner() == this ? (FieldEntry)field : new FieldEntry(this, field, this.tree.getSrcNsEquivalent(field));
            if (this.fields == null) {
                this.fields = new LinkedHashMap<MemberKey, FieldEntry>();
                this.fieldsView = Collections.unmodifiableCollection(this.fields.values());
            }
            return this.addMember(entry, this.fields, 1, 2);
        }

        @Override
        @Nullable
        public FieldEntry removeField(String srcName, @Nullable String srcDesc) {
            this.tree.assertNotInVisitPass();
            FieldEntry ret = this.getField(srcName, srcDesc);
            if (ret != null) {
                this.fields.remove(ret.getKey());
            }
            return ret;
        }

        public Collection<MethodEntry> getMethods() {
            if (this.methods == null) {
                return Collections.emptyList();
            }
            return this.methodsView;
        }

        @Override
        @Nullable
        public MethodEntry getMethod(String srcName, @Nullable String srcDesc) {
            return ClassEntry.getMember(srcName, srcDesc, this.methods, this.flags, 4, 8);
        }

        @Override
        @Nullable
        public MethodEntry getMethod(String name, @Nullable String desc, int namespace) {
            return (MethodEntry)MappingTree.ClassMapping.super.getMethod(name, desc, namespace);
        }

        @Override
        public MethodEntry addMethod(MappingTree.MethodMapping method) {
            this.tree.assertNotInVisitPass();
            return this.addMethodInternal(method);
        }

        MethodEntry addMethodInternal(MappingTree.MethodMapping method) {
            MethodEntry entry;
            MethodEntry methodEntry = entry = method instanceof MethodEntry && method.getOwner() == this ? (MethodEntry)method : new MethodEntry(this, method, this.tree.getSrcNsEquivalent(method));
            if (this.methods == null) {
                this.methods = new LinkedHashMap<MemberKey, MethodEntry>();
                this.methodsView = Collections.unmodifiableCollection(this.methods.values());
            }
            return this.addMember(entry, this.methods, 4, 8);
        }

        @Override
        @Nullable
        public MethodEntry removeMethod(String srcName, @Nullable String srcDesc) {
            this.tree.assertNotInVisitPass();
            MethodEntry ret = this.getMethod(srcName, srcDesc);
            if (ret != null) {
                this.methods.remove(ret.getKey());
            }
            return ret;
        }

        private static <T extends MemberEntry<T>> T getMember(String srcName, @Nullable String srcDesc, @Nullable Map<MemberKey, T> map, int flags, int flagHasAny, int flagMissesAny) {
            block13: {
                MemberEntry ret;
                boolean missedAnyDesc;
                boolean hasAnyDesc;
                block14: {
                    block12: {
                        Object ret2;
                        if (map == null) {
                            return null;
                        }
                        hasAnyDesc = (flags & flagHasAny) != 0;
                        boolean bl = missedAnyDesc = (flags & flagMissesAny) != 0;
                        if (srcDesc != null) break block12;
                        if (missedAnyDesc && (ret2 = (MemberEntry)map.get(new MemberKey(srcName, null))) != null) {
                            return (T)ret2;
                        }
                        if (!hasAnyDesc) break block13;
                        for (MemberEntry entry : map.values()) {
                            if (!entry.getSrcName().equals(srcName)) continue;
                            return (T)entry;
                        }
                        break block13;
                    }
                    if (!srcDesc.endsWith(")")) break block14;
                    if (missedAnyDesc) {
                        Object ret3 = (MemberEntry)map.get(new MemberKey(srcName, srcDesc));
                        if (ret3 != null) {
                            return (T)ret3;
                        }
                        ret3 = (MemberEntry)map.get(new MemberKey(srcName, null));
                        if (ret3 != null) {
                            return (T)ret3;
                        }
                    }
                    if (!hasAnyDesc) break block13;
                    for (MemberEntry entry : map.values()) {
                        if (!entry.getSrcName().equals(srcName) || !entry.srcDesc.startsWith(srcDesc)) continue;
                        return (T)entry;
                    }
                    break block13;
                }
                if (hasAnyDesc && (ret = (MemberEntry)map.get(new MemberKey(srcName, srcDesc))) != null) {
                    return (T)ret;
                }
                if (missedAnyDesc) {
                    ret = (MemberEntry)map.get(new MemberKey(srcName, null));
                    if (ret != null) {
                        return (T)ret;
                    }
                    if (srcDesc.indexOf(41) >= 0) {
                        for (MemberEntry entry : map.values()) {
                            if (!entry.getSrcName().equals(srcName) || !srcDesc.startsWith(entry.srcDesc)) continue;
                            return (T)entry;
                        }
                    }
                }
            }
            return null;
        }

        private <T extends MemberEntry<T>> T addMember(T entry, Map<MemberKey, T> map, int flagHasAny, int flagMissesAny) {
            T ret = map.putIfAbsent(entry.getKey(), entry);
            if (ret != null) {
                ret.copyFrom(entry, true);
                return ret;
            }
            if (MemoryMappingTree.isValidDescriptor(entry.srcDesc, true)) {
                this.flags = (byte)(this.flags | flagHasAny);
                if ((this.flags & flagMissesAny) != 0 && (ret = map.remove(new MemberKey(entry.getSrcName(), null))) != null) {
                    ret.setKey(entry.getKey());
                    ret.srcDesc = entry.srcDesc;
                    map.put(ret.getKey(), ret);
                    ret.copyFrom(entry, true);
                    entry = ret;
                }
                return entry;
            }
            if ((this.flags & flagHasAny) != 0) {
                for (T prevEntry : map.values()) {
                    if (prevEntry == entry || !prevEntry.getSrcName().equals(entry.getSrcName()) || entry.srcDesc != null && !prevEntry.srcDesc.startsWith(entry.srcDesc)) continue;
                    map.remove(entry.getKey());
                    prevEntry.copyFrom(entry, true);
                    return prevEntry;
                }
            }
            this.flags = (byte)(this.flags | flagMissesAny);
            return entry;
        }

        void accept(MappingVisitor visitor, VisitOrder order, boolean supplyFieldDstDescs, boolean supplyMethodDstDescs) throws IOException {
            if (visitor.visitClass(this.getSrcName()) && this.acceptElement(visitor, null)) {
                boolean methodsFirst;
                boolean bl = methodsFirst = order.isMethodsFirst() && this.fields != null && this.methods != null;
                if (!methodsFirst && this.fields != null) {
                    for (FieldEntry field : order.sortFields(this.fields.values())) {
                        field.accept(visitor, supplyFieldDstDescs);
                    }
                }
                if (this.methods != null) {
                    for (MethodEntry method : order.sortMethods(this.methods.values())) {
                        method.accept(visitor, order, supplyMethodDstDescs);
                    }
                }
                if (methodsFirst) {
                    for (FieldEntry field : order.sortFields(this.fields.values())) {
                        field.accept(visitor, supplyFieldDstDescs);
                    }
                }
            }
        }

        @Override
        protected void copyFrom(ClassEntry o, boolean replace) {
            super.copyFrom(o, replace);
            if (o.fields != null) {
                for (FieldEntry oField : o.fields.values()) {
                    FieldEntry field = this.getField(oField.getSrcName(), oField.srcDesc);
                    if (field == null) {
                        this.addFieldInternal(oField);
                        continue;
                    }
                    if (oField.srcDesc != null && field.srcDesc == null) {
                        this.fields.remove(field.getKey());
                        field.setKey(oField.getKey());
                        field.srcDesc = oField.srcDesc;
                        this.fields.put(field.getKey(), field);
                        this.flags = (byte)(this.flags | 1);
                    }
                    field.copyFrom(oField, replace);
                }
            }
            if (o.methods != null) {
                for (MethodEntry oMethod : o.methods.values()) {
                    MethodEntry method = this.getMethod(oMethod.getSrcName(), oMethod.srcDesc);
                    if (method == null) {
                        this.addMethodInternal(oMethod);
                        continue;
                    }
                    if (oMethod.srcDesc != null && method.srcDesc == null) {
                        this.methods.remove(method.getKey());
                        method.setKey(oMethod.getKey());
                        method.srcDesc = oMethod.srcDesc;
                        this.methods.put(method.getKey(), method);
                        this.flags = (byte)(this.flags | 4);
                    }
                    method.copyFrom(oMethod, replace);
                }
            }
        }

        public String toString() {
            return this.getSrcNameUnchecked();
        }
    }

    static final class FieldEntry
    extends MemberEntry<FieldEntry>
    implements MappingTree.FieldMapping {
        FieldEntry(ClassEntry owner, String srcName, @Nullable String srcDesc) {
            super(owner, srcName, srcDesc);
        }

        FieldEntry(ClassEntry owner, MappingTree.FieldMapping src, int srcNsEquivalent) {
            super(owner, src, srcNsEquivalent);
        }

        @Override
        public MappedElementKind getKind() {
            return MappedElementKind.FIELD;
        }

        @Override
        public void setSrcDesc(@Nullable String desc) {
            this.tree.assertNotInVisitPass();
            this.setSrcDescInternal(desc);
        }

        @Override
        void setSrcDescInternal(@Nullable String desc) {
            if (Objects.equals(desc, this.srcDesc)) {
                return;
            }
            MemberKey newKey = new MemberKey(this.getSrcName(), desc);
            if (this.owner.fields != null) {
                if (this.owner.fields.containsKey(newKey)) {
                    throw new IllegalArgumentException("conflicting name+desc after changing desc to " + desc + " for " + this);
                }
                this.owner.fields.remove(this.getKey());
            }
            this.srcDesc = desc;
            this.setKey(newKey);
            if (this.owner.fields != null) {
                this.owner.fields.put(newKey, this);
            }
            if (desc != null) {
                this.owner.flags = (byte)(this.owner.flags | 1);
            } else {
                this.owner.flags = (byte)(this.owner.flags | 2);
            }
        }

        void accept(MappingVisitor visitor, boolean supplyDstDescs) throws IOException {
            if (visitor.visitField(this.getSrcName(), this.srcDesc)) {
                this.acceptMember(visitor, supplyDstDescs);
            }
        }

        public String toString() {
            return String.format("%s;;%s", this.getSrcNameUnchecked(), this.srcDesc);
        }
    }

    static final class MethodEntry
    extends MemberEntry<MethodEntry>
    implements MappingTree.MethodMapping {
        private List<MethodArgEntry> args = null;
        private List<MethodVarEntry> vars = null;
        private List<MethodArgEntry> argsView = null;
        private List<MethodVarEntry> varsView = null;

        MethodEntry(ClassEntry owner, String srcName, @Nullable String srcDesc) {
            super(owner, srcName, srcDesc);
        }

        MethodEntry(ClassEntry owner, MappingTree.MethodMapping src, int srcNsEquivalent) {
            super(owner, src, srcNsEquivalent);
            for (MappingTree.MethodArgMapping methodArgMapping : src.getArgs()) {
                this.addArgInternal(methodArgMapping);
            }
            for (MappingTree.MethodVarMapping methodVarMapping : src.getVars()) {
                this.addVarInternal(methodVarMapping);
            }
        }

        @Override
        public MappedElementKind getKind() {
            return MappedElementKind.METHOD;
        }

        @Override
        public void setSrcDesc(@Nullable String desc) {
            this.tree.assertNotInVisitPass();
            this.setSrcDescInternal(desc);
        }

        @Override
        void setSrcDescInternal(@Nullable String desc) {
            if (Objects.equals(desc, this.srcDesc)) {
                return;
            }
            MemberKey newKey = new MemberKey(this.getSrcName(), desc);
            if (this.owner.methods != null) {
                if (this.owner.methods.containsKey(newKey)) {
                    throw new IllegalArgumentException("conflicting name+desc after changing desc to " + desc + " for " + this);
                }
                this.owner.methods.remove(this.getKey());
            }
            this.srcDesc = desc;
            this.setKey(newKey);
            if (this.owner.methods != null) {
                this.owner.methods.put(newKey, this);
            }
            if (MemoryMappingTree.isValidDescriptor(desc, true)) {
                this.owner.flags = (byte)(this.owner.flags | 4);
            } else {
                this.owner.flags = (byte)(this.owner.flags | 8);
            }
        }

        public Collection<MethodArgEntry> getArgs() {
            if (this.args == null) {
                return Collections.emptyList();
            }
            return this.argsView;
        }

        @Override
        @Nullable
        public MethodArgEntry getArg(int argPosition, int lvIndex, @Nullable String srcName) {
            if (this.args == null) {
                return null;
            }
            if (argPosition >= 0 || lvIndex >= 0) {
                for (MethodArgEntry entry : this.args) {
                    if ((argPosition < 0 || entry.argPosition != argPosition) && (lvIndex < 0 || entry.lvIndex != lvIndex) || srcName != null && entry.getSrcName() != null && !srcName.equals(entry.getSrcName())) continue;
                    return entry;
                }
            }
            if (srcName != null) {
                for (MethodArgEntry entry : this.args) {
                    if (!srcName.equals(entry.getSrcName()) || argPosition >= 0 && entry.argPosition >= 0 || lvIndex >= 0 && entry.lvIndex >= 0) continue;
                    return entry;
                }
            }
            return null;
        }

        @Override
        public MethodArgEntry addArg(MappingTree.MethodArgMapping arg) {
            this.tree.assertNotInVisitPass();
            return this.addArgInternal(arg);
        }

        MethodArgEntry addArgInternal(MappingTree.MethodArgMapping arg) {
            MethodArgEntry entry = arg instanceof MethodArgEntry && arg.getMethod() == this ? (MethodArgEntry)arg : new MethodArgEntry(this, arg, this.owner.tree.getSrcNsEquivalent(arg));
            MethodArgEntry prev = this.getArg(arg.getArgPosition(), arg.getLvIndex(), arg.getSrcName());
            if (prev == null) {
                if (this.args == null) {
                    this.args = new ArrayList<MethodArgEntry>();
                    this.argsView = Collections.unmodifiableList(this.args);
                }
                this.args.add(entry);
            } else {
                prev.copyFrom(entry, true);
            }
            return entry;
        }

        @Override
        @Nullable
        public MethodArgEntry removeArg(int argPosition, int lvIndex, @Nullable String srcName) {
            this.tree.assertNotInVisitPass();
            MethodArgEntry ret = this.getArg(argPosition, lvIndex, srcName);
            if (ret != null) {
                this.args.remove(ret);
            }
            return ret;
        }

        public Collection<MethodVarEntry> getVars() {
            if (this.vars == null) {
                return Collections.emptyList();
            }
            return this.varsView;
        }

        @Override
        @Nullable
        public MethodVarEntry getVar(int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, @Nullable String srcName) {
            boolean hasMissing;
            if (this.vars == null) {
                return null;
            }
            if (lvtRowIndex >= 0) {
                hasMissing = false;
                for (MethodVarEntry entry : this.vars) {
                    if (entry.lvtRowIndex == lvtRowIndex) {
                        return entry;
                    }
                    if (entry.lvtRowIndex >= 0) continue;
                    hasMissing = true;
                }
                if (!hasMissing) {
                    return null;
                }
            }
            if (lvIndex >= 0) {
                hasMissing = false;
                MethodVarEntry bestMatch = null;
                for (MethodVarEntry entry : this.vars) {
                    if (lvtRowIndex >= 0 && entry.lvtRowIndex >= 0 && lvtRowIndex != entry.lvtRowIndex || srcName != null && entry.getSrcName() != null && !srcName.equals(entry.getSrcName())) continue;
                    if (entry.lvIndex != lvIndex) {
                        if (entry.lvIndex >= 0) continue;
                        hasMissing = true;
                        continue;
                    }
                    if (startOpIdx >= 0 && endOpIdx >= 0 && entry.startOpIdx >= 0 && entry.endOpIdx >= 0) {
                        if (startOpIdx >= entry.endOpIdx || endOpIdx <= entry.startOpIdx) continue;
                        return entry;
                    }
                    if (endOpIdx >= 0 && entry.startOpIdx >= 0 && endOpIdx <= entry.startOpIdx || entry.endOpIdx >= 0 && startOpIdx >= 0 && entry.endOpIdx <= startOpIdx) continue;
                    if (startOpIdx < 0 || startOpIdx == entry.startOpIdx) {
                        return entry;
                    }
                    if (bestMatch != null && (entry.startOpIdx < 0 || Math.abs(entry.startOpIdx - startOpIdx) >= Math.abs(bestMatch.startOpIdx - startOpIdx))) continue;
                    bestMatch = entry;
                }
                if (!hasMissing || bestMatch != null) {
                    return bestMatch;
                }
            }
            if (srcName != null) {
                for (MethodVarEntry entry : this.vars) {
                    if (!srcName.equals(entry.getSrcName()) || lvtRowIndex >= 0 && entry.lvtRowIndex >= 0 || lvIndex >= 0 && entry.lvIndex >= 0) continue;
                    return entry;
                }
            }
            return null;
        }

        @Override
        public MethodVarEntry addVar(MappingTree.MethodVarMapping var) {
            this.tree.assertNotInVisitPass();
            return this.addVarInternal(var);
        }

        MethodVarEntry addVarInternal(MappingTree.MethodVarMapping var) {
            MethodVarEntry entry = var instanceof MethodVarEntry && var.getMethod() == this ? (MethodVarEntry)var : new MethodVarEntry(this, var, this.owner.tree.getSrcNsEquivalent(var));
            MethodVarEntry prev = this.getVar(var.getLvtRowIndex(), var.getLvIndex(), var.getStartOpIdx(), var.getEndOpIdx(), var.getSrcName());
            if (prev == null) {
                if (this.vars == null) {
                    this.vars = new ArrayList<MethodVarEntry>();
                    this.varsView = Collections.unmodifiableList(this.vars);
                }
                this.vars.add(entry);
            } else {
                prev.copyFrom(entry, true);
            }
            return entry;
        }

        @Override
        @Nullable
        public MethodVarEntry removeVar(int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, @Nullable String srcName) {
            this.tree.assertNotInVisitPass();
            MethodVarEntry ret = this.getVar(lvtRowIndex, lvIndex, startOpIdx, endOpIdx, srcName);
            if (ret != null) {
                this.vars.remove(ret);
            }
            return ret;
        }

        void accept(MappingVisitor visitor, VisitOrder order, boolean supplyDstDescs) throws IOException {
            if (visitor.visitMethod(this.getSrcName(), this.srcDesc) && this.acceptMember(visitor, supplyDstDescs)) {
                boolean varsFirst;
                boolean bl = varsFirst = order.isMethodVarsFirst() && this.args != null && this.vars != null;
                if (!varsFirst && this.args != null) {
                    for (MethodArgEntry arg : order.sortMethodArgs(this.args)) {
                        arg.accept(visitor);
                    }
                }
                if (this.vars != null) {
                    for (MethodVarEntry var : order.sortMethodVars(this.vars)) {
                        var.accept(visitor);
                    }
                }
                if (varsFirst) {
                    for (MethodArgEntry arg : order.sortMethodArgs(this.args)) {
                        arg.accept(visitor);
                    }
                }
            }
        }

        @Override
        protected void copyFrom(MethodEntry o, boolean replace) {
            super.copyFrom(o, replace);
            if (o.args != null) {
                for (MethodArgEntry oArg : o.args) {
                    MethodArgEntry arg = this.getArg(oArg.argPosition, oArg.lvIndex, oArg.getSrcName());
                    if (arg == null) {
                        this.addArgInternal(oArg);
                        continue;
                    }
                    arg.copyFrom(oArg, replace);
                }
            }
            if (o.vars != null) {
                for (MethodVarEntry oVar : o.vars) {
                    MethodVarEntry var = this.getVar(oVar.lvtRowIndex, oVar.lvIndex, oVar.startOpIdx, oVar.endOpIdx, oVar.getSrcName());
                    if (var == null) {
                        this.addVarInternal(oVar);
                        continue;
                    }
                    var.copyFrom(oVar, replace);
                }
            }
        }

        public String toString() {
            return String.format("%s%s", this.getSrcNameUnchecked(), this.srcDesc);
        }
    }

    static final class MethodArgEntry
    extends Entry<MethodArgEntry>
    implements MappingTree.MethodArgMapping {
        private final MethodEntry method;
        private int argPosition;
        private int lvIndex;

        MethodArgEntry(MethodEntry method, int argPosition, int lvIndex, @Nullable String srcName) {
            super(method.owner.tree, srcName);
            this.method = method;
            this.argPosition = argPosition;
            this.lvIndex = lvIndex;
        }

        MethodArgEntry(MethodEntry method, MappingTree.MethodArgMapping src, int srcNsEquivalent) {
            super(method.owner.tree, src, srcNsEquivalent);
            this.method = method;
            this.argPosition = src.getArgPosition();
            this.lvIndex = src.getLvIndex();
        }

        @Override
        public MappingTree getTree() {
            return this.method.owner.tree;
        }

        @Override
        public MappedElementKind getKind() {
            return MappedElementKind.METHOD_ARG;
        }

        @Override
        public MethodEntry getMethod() {
            return this.method;
        }

        @Override
        public int getArgPosition() {
            return this.argPosition;
        }

        @Override
        public void setArgPosition(int position) {
            this.tree.assertNotInVisitPass();
            this.setArgPositionInternal(position);
        }

        void setArgPositionInternal(int position) {
            this.argPosition = position;
        }

        @Override
        public int getLvIndex() {
            return this.lvIndex;
        }

        @Override
        public void setLvIndex(int index) {
            this.tree.assertNotInVisitPass();
            this.setLvIndexInternal(index);
        }

        void setLvIndexInternal(int index) {
            this.lvIndex = index;
        }

        void accept(MappingVisitor visitor) throws IOException {
            if (visitor.visitMethodArg(this.argPosition, this.lvIndex, this.getSrcName())) {
                this.acceptElement(visitor, null);
            }
        }

        @Override
        protected void copyFrom(MethodArgEntry o, boolean replace) {
            if (o.argPosition >= 0 && (replace || this.argPosition < 0)) {
                this.setArgPositionInternal(o.argPosition);
            }
            if (o.lvIndex >= 0 && (replace || this.lvIndex < 0)) {
                this.setLvIndexInternal(o.getLvIndex());
            }
            if (o.getSrcName() != null && (replace || this.getSrcName() == null)) {
                this.setSrcName(o.getSrcName());
            }
            super.copyFrom(o, replace);
        }

        public String toString() {
            return String.format("%d/%d:%s", this.argPosition, this.lvIndex, this.getSrcName());
        }
    }

    static final class MethodVarEntry
    extends Entry<MethodVarEntry>
    implements MappingTree.MethodVarMapping {
        private final MethodEntry method;
        private int lvtRowIndex;
        private int lvIndex;
        private int startOpIdx;
        private int endOpIdx;

        MethodVarEntry(MethodEntry method, int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, @Nullable String srcName) {
            super(method.owner.tree, srcName);
            this.method = method;
            this.lvtRowIndex = lvtRowIndex;
            this.lvIndex = lvIndex;
            this.startOpIdx = startOpIdx;
            this.endOpIdx = endOpIdx;
        }

        MethodVarEntry(MethodEntry method, MappingTree.MethodVarMapping src, int srcNs) {
            super(method.owner.tree, src, srcNs);
            this.method = method;
            this.lvtRowIndex = src.getLvtRowIndex();
            this.lvIndex = src.getLvIndex();
            this.startOpIdx = src.getStartOpIdx();
            this.endOpIdx = src.getEndOpIdx();
        }

        @Override
        public MappingTree getTree() {
            return this.method.owner.tree;
        }

        @Override
        public MappedElementKind getKind() {
            return MappedElementKind.METHOD_VAR;
        }

        @Override
        public MethodEntry getMethod() {
            return this.method;
        }

        @Override
        public int getLvtRowIndex() {
            return this.lvtRowIndex;
        }

        @Override
        public void setLvtRowIndex(int index) {
            this.tree.assertNotInVisitPass();
            this.setLvtRowIndexInternal(index);
        }

        void setLvtRowIndexInternal(int index) {
            this.lvtRowIndex = index;
        }

        @Override
        public int getLvIndex() {
            return this.lvIndex;
        }

        @Override
        public int getStartOpIdx() {
            return this.startOpIdx;
        }

        @Override
        public int getEndOpIdx() {
            return this.endOpIdx;
        }

        @Override
        public void setLvIndex(int lvIndex, int startOpIdx, int endOpIdx) {
            this.tree.assertNotInVisitPass();
            this.setLvIndexInternal(lvIndex, startOpIdx, endOpIdx);
        }

        void setLvIndexInternal(int lvIndex, int startOpIdx, int endOpIdx) {
            this.lvIndex = lvIndex;
            this.startOpIdx = startOpIdx;
            this.endOpIdx = endOpIdx;
        }

        void accept(MappingVisitor visitor) throws IOException {
            if (visitor.visitMethodVar(this.lvtRowIndex, this.lvIndex, this.startOpIdx, this.endOpIdx, this.getSrcName())) {
                this.acceptElement(visitor, null);
            }
        }

        @Override
        protected void copyFrom(MethodVarEntry o, boolean replace) {
            if (o.lvtRowIndex >= 0 && (replace || this.lvtRowIndex < 0)) {
                this.setLvtRowIndexInternal(o.lvtRowIndex);
            }
            if (o.lvIndex >= 0 && o.startOpIdx >= 0 && (replace || this.lvIndex < 0 || this.startOpIdx < 0)) {
                this.setLvIndexInternal(o.lvIndex, o.startOpIdx, o.endOpIdx);
            }
            if (o.getSrcName() != null && (replace || this.getSrcName() == null)) {
                this.setSrcName(o.getSrcName());
            }
            super.copyFrom(o, replace);
        }

        public String toString() {
            return String.format("%d/%d@%d-%d:%s", this.lvtRowIndex, this.lvIndex, this.startOpIdx, this.endOpIdx, this.getSrcName());
        }
    }

    static abstract class Entry<T extends Entry<T>>
    implements MappingTree.ElementMapping {
        private final boolean missingSrcNameAllowed;
        protected final MemoryMappingTree tree;
        private String srcName;
        protected String[] dstNames;
        protected String comment;

        protected Entry(MemoryMappingTree tree, String srcName) {
            this.missingSrcNameAllowed = this.getKind().level > MappedElementKind.METHOD.level;
            this.tree = tree;
            this.srcName = srcName;
            this.dstNames = new String[tree.dstNamespaces.size()];
        }

        protected Entry(MemoryMappingTree tree, MappingTree.ElementMapping src, int srcNsEquivalent) {
            this(tree, src.getName(srcNsEquivalent));
            for (int i = 0; i < this.dstNames.length; ++i) {
                int dstNsEquivalent = src.getTree().getNamespaceId((String)tree.dstNamespaces.get(i));
                if (dstNsEquivalent == -2) continue;
                this.setDstNameInternal(src.getDstName(dstNsEquivalent), i);
            }
            this.setCommentInternal(src.getComment());
        }

        public abstract MappedElementKind getKind();

        final boolean isSrcNameMissing() {
            return this.srcName == null;
        }

        String getSrcNameUnchecked() {
            return this.srcName;
        }

        @Override
        public final String getSrcName() {
            if (!this.missingSrcNameAllowed) {
                this.assertSrcNamePresent();
            }
            return this.srcName;
        }

        protected final void assertSrcNamePresent() {
            if (this.isSrcNameMissing()) {
                throw new UnsupportedOperationException("Attempted illegal interaction with a pending entry still missing its tree-side source name");
            }
        }

        void setSrcName(String name) {
            if (!this.missingSrcNameAllowed && name == null) {
                throw new UnsupportedOperationException("Source name cannot be null");
            }
            this.srcName = name;
        }

        @Override
        @Nullable
        public final String getDstName(int namespace) {
            return this.dstNames[namespace];
        }

        @Override
        public final void setDstName(String name, int namespace) {
            this.tree.assertNotInVisitPass();
            this.setDstNameInternal(name, namespace);
        }

        void setDstNameInternal(String name, int namespace) {
            this.dstNames[namespace] = name;
        }

        void resizeDstNames(int newSize) {
            this.dstNames = Arrays.copyOf(this.dstNames, newSize);
        }

        void updateDstNames(int[] map) {
            String[] newDstNames = new String[map.length];
            for (int i = 0; i < map.length; ++i) {
                int src = map[i];
                if (src < 0) continue;
                newDstNames[i] = this.dstNames[src];
            }
            this.dstNames = newDstNames;
        }

        @Override
        @Nullable
        public final String getComment() {
            return this.comment;
        }

        @Override
        public final void setComment(String comment) {
            this.tree.assertNotInVisitPass();
            this.setCommentInternal(comment);
        }

        void setCommentInternal(String comment) {
            this.comment = comment;
        }

        protected final boolean acceptElement(MappingVisitor visitor, @Nullable String[] dstDescs) throws IOException {
            int i;
            MappedElementKind kind = this.getKind();
            for (i = 0; i < this.dstNames.length; ++i) {
                String dstName = this.dstNames[i];
                if (dstName == null) continue;
                visitor.visitDstName(kind, i, dstName);
            }
            if (dstDescs != null) {
                for (i = 0; i < dstDescs.length; ++i) {
                    String dstDesc = dstDescs[i];
                    if (dstDesc == null) continue;
                    visitor.visitDstDesc(kind, i, dstDesc);
                }
            }
            if (!visitor.visitElementContent(kind)) {
                return false;
            }
            if (this.comment != null) {
                visitor.visitComment(kind, this.comment);
            }
            return true;
        }

        protected void copyFrom(T o, boolean replace) {
            for (int i = 0; i < this.dstNames.length; ++i) {
                if (((Entry)o).dstNames[i] == null || !replace && this.dstNames[i] != null) continue;
                this.dstNames[i] = ((Entry)o).dstNames[i];
            }
            if (((Entry)o).comment != null && (replace || this.comment == null)) {
                this.comment = ((Entry)o).comment;
            }
        }
    }

    static final class MetadataEntryImpl
    implements MappingTree.MetadataEntry {
        final String key;
        final String value;

        MetadataEntryImpl(String key, @Nullable String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        @Nullable
        public String getValue() {
            return this.value;
        }

        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof MetadataEntryImpl)) {
                return false;
            }
            MetadataEntryImpl entry = (MetadataEntryImpl)other;
            return this.key.equals(entry.key) && Objects.equals(this.value, entry.value);
        }

        public int hashCode() {
            int ret = this.key.hashCode();
            if (this.value != null) {
                ret |= this.value.hashCode();
            }
            return ret;
        }

        public String toString() {
            return this.key + ":" + this.value;
        }
    }

    static abstract class MemberEntry<T extends MemberEntry<T>>
    extends Entry<T>
    implements MappingTree.MemberMapping {
        protected ClassEntry owner;
        protected String srcDesc;
        private MemberKey key;

        protected MemberEntry(ClassEntry owner, String srcName, @Nullable String srcDesc) {
            super(owner.tree, srcName);
            this.owner = owner;
            this.srcDesc = srcDesc;
            this.key = new MemberKey(srcName, srcDesc);
        }

        protected MemberEntry(ClassEntry owner, MappingTree.MemberMapping src, int srcNsEquivalent) {
            super(owner.tree, src, srcNsEquivalent);
            this.owner = owner;
            this.srcDesc = src.getDesc(srcNsEquivalent);
            this.key = new MemberKey(this.getSrcName(), this.srcDesc);
        }

        @Override
        public MappingTree getTree() {
            return this.owner.tree;
        }

        @Override
        public final ClassEntry getOwner() {
            return this.owner;
        }

        void setOwner(ClassEntry owner) {
            assert (this.tree.inVisitPass);
            assert (owner.getSrcName().equals(this.owner.getSrcName()));
            this.owner = owner;
        }

        @Override
        void setSrcName(String name) {
            assert (this.tree.inVisitPass);
            super.setSrcName(name);
            this.key = new MemberKey(name, this.srcDesc);
        }

        @Override
        @Nullable
        public final String getSrcDesc() {
            return this.srcDesc;
        }

        abstract void setSrcDescInternal(@Nullable String var1);

        MemberKey getKey() {
            this.assertSrcNamePresent();
            return this.key;
        }

        void setKey(MemberKey key) {
            this.key = key;
        }

        protected final boolean acceptMember(MappingVisitor visitor, boolean supplyDstDescs) throws IOException {
            String[] dstDescs;
            if (!supplyDstDescs || this.srcDesc == null) {
                dstDescs = null;
            } else {
                MemoryMappingTree tree = this.owner.tree;
                dstDescs = new String[tree.getDstNamespaces().size()];
                for (int i = 0; i < dstDescs.length; ++i) {
                    dstDescs[i] = tree.mapDesc(this.srcDesc, i);
                }
            }
            return this.acceptElement(visitor, dstDescs);
        }
    }

    static final class GlobalMemberKey {
        private final ClassEntry owner;
        private final String name;
        private final String desc;
        private final boolean isField;

        GlobalMemberKey(ClassEntry owner, String name, @Nullable String desc, boolean isField) {
            this.owner = owner;
            this.name = name;
            this.desc = desc;
            this.isField = isField;
        }

        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != GlobalMemberKey.class) {
                return false;
            }
            GlobalMemberKey o = (GlobalMemberKey)obj;
            return this.owner == o.owner && this.name.equals(o.name) && Objects.equals(this.desc, o.desc) && this.isField == o.isField;
        }

        public int hashCode() {
            int ret = this.owner.hashCode() * 31 + this.name.hashCode();
            if (this.desc != null) {
                ret |= this.desc.hashCode();
            }
            if (this.isField) {
                ++ret;
            }
            return ret;
        }

        public String toString() {
            return String.format("%s.%s.%s", this.owner, this.name, this.desc);
        }
    }

    static final class MemberKey {
        private final String name;
        private final String desc;
        private final int hash;

        MemberKey(@Nullable String name, @Nullable String desc) {
            this.name = name;
            this.desc = desc;
            this.hash = name == null ? super.hashCode() : (desc == null ? name.hashCode() : name.hashCode() * 257 + desc.hashCode());
        }

        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != MemberKey.class) {
                return false;
            }
            MemberKey o = (MemberKey)obj;
            if (this.name == null || o.name == null) {
                return false;
            }
            return Objects.equals(this.name, o.name) && Objects.equals(this.desc, o.desc);
        }

        public int hashCode() {
            return this.hash;
        }

        public String toString() {
            return String.format("%s.%s", this.name, this.desc);
        }
    }
}

