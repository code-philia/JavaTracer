package org.cophi.javatracer.model.variables;

import java.util.List;
import org.cophi.javatracer.algorithm.GraphNode;

/**
 * @author LLT
 */
public class BreakPointValue extends VarValue {

    private static final long serialVersionUID = -8762384056186966652L;
    private String name;

    public BreakPointValue(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfAvailableVariables() {
        if (children == null || children.isEmpty()) {
            return 0;
        } else {
            return children.size();
        }
    }

    @Override
    public boolean isTheSameWith(GraphNode nodeAfter) {
        return true;
    }

//	public String getBkpId() {
//		return getVariablePath();
//	}

    public void setChildren(List<VarValue> children) {
        this.children = children;
    }

//	@Override
//	public String getChildId(String childCode) {
//		return childCode;
//	}

    @Override
    public boolean match(GraphNode node) {
        return true;
    }

//	public Double getValue(final String variableId, Double defaultIfNull) {
//		Double value = getValue(variableId, this);
//		if (value != null) {
//			return value;
//		}
//		return defaultIfNull;
//	}
//
//	private Double getValue(final String variableId, final ExecValue value) {
//		if (value.getVarId().equals(variableId)) {
//			return Double.valueOf(value.getDoubleVal());
//		} else {
//			for (ExecValue child : CollectionUtils.initIfEmpty(value.getChildren())) {
//				Double val = getValue(variableId, child);
//				if (val != null) {
//					return val;
//				}
//			}
//			return null;
//		}
//	}

//	public Set<String> getAllLabels() {
//		return getChildLabels(this);
//	}
//
//	private Set<String> getChildLabels(final ExecValue value) {
//		final Set<String> labels = new HashSet<String>();
//		if (value == null || value.getChildren() == null || value.getChildren().isEmpty()) {
//			labels.add(value.getVarId());
//		} else {
//			for (ExecValue child : value.getChildren()) {
//				labels.addAll(getChildLabels(child));
//			}
//		}
//		return labels;
//	}
//
//	public double[] getAllValues() {
//		List<Double> list = getChildValues(this);
//		double[] array = new double[list.size()];
//		int i = 0;
//		for (Double val : list) {
//			array[i++] = val.doubleValue();
//		}
//		return array;
//	}

//	private List<Double> getChildValues(final ExecValue value) {
//		if (value == null || value.getChildren() == null || value.getChildren().isEmpty()) {
//			return Arrays.asList(value.getDoubleVal());
//		} else {
//			List<Double> labels = new ArrayList<Double>();
//			for (ExecValue child : value.getChildren()) {
//				labels.addAll(getChildValues(child));
//			}
//			return labels;
//		}
//	}

    @Override
    public String getHeapID() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BreakPointValue) {
            BreakPointValue otherVal = (BreakPointValue) obj;
            return otherVal.name.equals(this.name);
        }

        return false;
    }

    @Override
    public BreakPointValue clone() {
        return this;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    protected boolean needToRetrieveValue() {
        return false;
    }
}
