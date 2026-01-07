package t.uni.system.core.template;

import t.uni.core.utils.processor.TreeProcessor;
import t.uni.domain.common.model.dto.excel.PermissionExcel;

import java.util.List;
import java.util.stream.Collectors;

public class PermissionTreeProcessor extends TreeProcessor<PermissionExcel> {
    @Override
    public List<PermissionExcel> findRoots(List<PermissionExcel> list) {
        return list.stream()
                .filter(p -> p.getParentId() == null || p.getParentId() == 0)
                .collect(Collectors.toList());
    }

    @Override
    public void buildChildren(PermissionExcel parent, List<PermissionExcel> list) {
        List<PermissionExcel> children = list.stream()
                .filter(p -> parent.getId().equals(p.getParentId()))
                .collect(Collectors.toList());

        if (!children.isEmpty()) {
            parent.setChildren(children);
            children.forEach(child -> buildChildren(child, list));
        }
    }
}