package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.adapter;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.MappingVisitor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MappingNsRenamer extends ForwardingMappingVisitor {
   private final Map<String, String> nameMap;

   public MappingNsRenamer(MappingVisitor next, Map<String, String> nameMap) {
      super(next);
      Objects.requireNonNull(nameMap, "null name map");
      this.nameMap = nameMap;
   }

   @Override
   public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
      String newSrcNamespace = this.nameMap.getOrDefault(srcNamespace, srcNamespace);
      List<String> newDstNamespaces = new ArrayList<>(dstNamespaces.size());

      for (String ns : dstNamespaces) {
         newDstNamespaces.add(this.nameMap.getOrDefault(ns, ns));
      }

      super.visitNamespaces(newSrcNamespace, newDstNamespaces);
   }
}
