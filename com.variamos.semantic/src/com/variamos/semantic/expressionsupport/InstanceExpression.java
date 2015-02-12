package com.variamos.semantic.expressionsupport;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.variamos.hlcl.BooleanExpression;
import com.variamos.hlcl.DomainParser;
import com.variamos.hlcl.Expression;
import com.variamos.hlcl.HlclFactory;
import com.variamos.hlcl.Identifier;
import com.variamos.hlcl.RangeDomain;
import com.variamos.semantic.semanticsupport.SemanticVariable;
import com.variamos.semantic.types.ExpressionVertexType;
import com.variamos.syntax.instancesupport.InstElement;
import com.variamos.syntax.instancesupport.InstVertex;
import com.variamos.syntax.metamodelsupport.AbstractAttribute;
import com.variamos.syntax.semanticinterface.IntInstanceExpression;

/**
 * A class to represent InstanceExpressions. Part of PhD work at University of
 * Paris 1
 * 
 * @author Juan C. Mu�oz Fern�ndez <jcmunoz@gmail.com>
 * 
 * @version 1.1
 * @since 2014-02-05
 */
/**
 * @author jcmunoz
 *
 */
public class InstanceExpression implements Serializable, IntInstanceExpression {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6296982198124042304L;
	private static HlclFactory hlclFactory = new HlclFactory();

	/**
	 * Associated SemanticExpression: volatile for semantic SemanticExpression; custom
	 * for variable expressions or InstElement custom expressions
	 */
	private SemanticExpression volatileSemanticExpression;
	private SemanticExpression customSemanticExpression;

	/**
	 * for LEFT
	 */
	private InstElement volatieLeftElement;

	/**
	 * for RIGHT
	 */
	private InstElement volatileRightElement;

	/**
	 * for LEFTSUBEXPRESSION
	 */
	private InstanceExpression leftInstanceExpression;
	/**
	 * for RIGHTSUBEXPRESSION
	 */
	private InstanceExpression rightInstanceExpression;

	// TODO change to a new class for type (normal, relax)
	private List<ExpressionSubAction> expressionSubActions;

	/**
	 * For LEFTVARIABLEVALUE
	 */
	private String leftValue;

	/**
	 * For RIGHTVARIABLEVALUE
	 */
	private String rightValue;

	private String lastLeft = null;
	private String lastRight = null;

	private boolean customExpression;
	private String semanticExpressionId;

	private String rightElementId;
	private String leftElementId;

	public String getLastLeft() {
		return lastLeft;
	}

	public void setLastLeft(String lastLeft) {
		this.lastLeft = lastLeft;
	}

	public String getLastRight() {
		return lastRight;
	}

	public void setLastRight(String lastRight) {
		this.lastRight = lastRight;
	}

	public InstanceExpression() {
		customExpression = false;
	}

	public InstanceExpression(boolean customExpression, String id) {
		this.customExpression = customExpression;
		if (customExpression) {
			customSemanticExpression = new SemanticExpression(id);
			semanticExpressionId = id;
		}

	}

	public InstanceExpression(boolean customExpression,
			SemanticExpression semanticExpression) {
		this.customExpression = customExpression;
		if (customExpression) {
			customSemanticExpression = semanticExpression;
			semanticExpressionId = semanticExpression.getIdentifier();
		}
	}

	public InstanceExpression(SemanticExpression semanticExpression, InstElement left,
			InstElement right) {
		this.volatileSemanticExpression = semanticExpression;
		this.semanticExpressionId = semanticExpression.getIdentifier();
		setLeftElement(left);
		setRightElement(right);
	}

	public InstanceExpression(SemanticExpression semanticExpression,
			InstElement vertex, boolean replaceTarget,
			InstanceExpression instanceExpression) {
		this.volatileSemanticExpression = semanticExpression;
		this.semanticExpressionId = semanticExpression.getIdentifier();
		if (replaceTarget) {
			setLeftElement(vertex);
			this.rightInstanceExpression = instanceExpression;
		} else {
			setRightElement(vertex);
			this.leftInstanceExpression = instanceExpression;
		}
	}

	public InstanceExpression(SemanticExpression semanticExpression,
			InstanceExpression leftInstanceExpression,
			InstanceExpression rightInstanceExpression) {
		this.volatileSemanticExpression = semanticExpression;
		this.semanticExpressionId = semanticExpression.getIdentifier();
		this.leftInstanceExpression = leftInstanceExpression;
		this.rightInstanceExpression = rightInstanceExpression;
	}

	public InstanceExpression(SemanticExpression semanticExpression, InstElement vertex) {
		this.volatileSemanticExpression = semanticExpression;
		this.semanticExpressionId = semanticExpression.getIdentifier();
		setLeftElement(vertex);
	}
	
	public Expression createSGSExpression(String element)
	{
		Expression condition = createExpression();
		Identifier iden = hlclFactory.newIdentifier(element+"_Selected");
		return hlclFactory.doubleImplies(iden, (BooleanExpression)condition);
	}

	public Expression createExpression() {
		List<Expression> expressionTerms = expressionTerms();
		Class<? extends HlclFactory> hlclFactoryClass = hlclFactory.getClass();
		SemanticExpressionType semanticExpressionType = getSemanticExpression()
				.getSemanticExpressionType();
		boolean singleParameter = semanticExpressionType.isSingleInExpression();
		boolean arrayParameters = semanticExpressionType.isArrayParameters();
		Class<? extends Expression> parameter1 = null, parameter2 = null;
		parameter1 = getSemanticExpression().getSemanticExpressionType()
				.getLeftExpressionClass();
		if (!singleParameter)
			parameter2 = getSemanticExpression().getSemanticExpressionType()
					.getRightExpressionClass();
		Method factoryMethod = null;
		try {
			if (singleParameter) {
				// For negation, literal and number expressions
				factoryMethod = hlclFactoryClass.getMethod(
						semanticExpressionType.getMethod(), parameter1);
				factoryMethod.invoke(hlclFactory,
						parameter1.cast(expressionTerms.get(0)));
			} else if (arrayParameters) {
				// For Sum and Product Expressions
				Object dynamicArrayObject = Array.newInstance(parameter1, 2);

				Object[] dynamicArrayCast = (Object[]) dynamicArrayObject;
				dynamicArrayCast[0] = parameter1.cast(expressionTerms.get(0));
				dynamicArrayCast[1] = parameter2.cast(expressionTerms.get(1));

				factoryMethod = hlclFactoryClass.getMethod(
						semanticExpressionType.getMethod(),
						dynamicArrayObject.getClass());
				return (Expression) factoryMethod.invoke(hlclFactory,
						dynamicArrayObject);

			} else {
				// For the other expressions
				factoryMethod = hlclFactoryClass.getMethod(
						semanticExpressionType.getMethod(), parameter1, parameter2);

				return (Expression) factoryMethod.invoke(hlclFactory,
						parameter1.cast(expressionTerms.get(0)),
						parameter2.cast(expressionTerms.get(1)));
			}
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Map<String, Identifier> getIdentifiers() {
		Map<String, Identifier> out = new HashMap<String, Identifier>();
		if (volatieLeftElement != null
				&& getSemanticExpression().getLeftAttributeName() != null) {
			// System.out.println(leftVertex.getIdentifier() + " "
			// + leftAttributeName);
			Identifier identifier = hlclFactory.newIdentifier(volatieLeftElement
					.getInstAttributeFullIdentifier(getSemanticExpression()
							.getLeftAttributeName()), getSemanticExpression()
					.getLeftAttributeName());
			out.put(volatieLeftElement
					.getInstAttributeFullIdentifier(getSemanticExpression()
							.getLeftAttributeName()), identifier);
			AbstractAttribute attribute = volatieLeftElement.getInstAttribute(
					getSemanticExpression().getLeftAttributeName()).getAttribute();
			if (attribute.getType().equals("Integer")) {
				if (attribute.getDomain() != null)
					identifier.setDomain(attribute.getDomain());
				else {
					if (attribute.getName().equals("value")) {
						String domain = (String) volatieLeftElement.getInstAttribute(
								SemanticVariable.VAR_VARIABLEDOMAIN).getValue();
						identifier.setDomain(DomainParser.parseDomain(domain));
					} else
						identifier.setDomain(new RangeDomain(0, 4));
				}
			}
		}
		if (volatileRightElement != null
				&& getSemanticExpression().getRightAttributeName() != null) {
			// System.out
			// .println(rightVertex.getIdentifier() + rightAttributeName);
			Identifier identifier = hlclFactory.newIdentifier(volatileRightElement
					.getInstAttributeFullIdentifier(getSemanticExpression()
							.getRightAttributeName()), getSemanticExpression()
					.getRightAttributeName());

			out.put(volatileRightElement
					.getInstAttributeFullIdentifier(getSemanticExpression()
							.getRightAttributeName()), identifier);
			AbstractAttribute attribute = volatileRightElement.getInstAttribute(
					getSemanticExpression().getRightAttributeName()).getAttribute();
			if (attribute.getType().equals("Integer")) {
				if (attribute.getDomain() != null)
					identifier.setDomain(attribute.getDomain());
				else if (attribute.getName().equals("value")) {
					String domain = (String) volatileRightElement.getInstAttribute(
							SemanticVariable.VAR_VARIABLEDOMAIN).getValue();
					identifier.setDomain(DomainParser.parseDomain(domain));
				} else
					identifier.setDomain(new RangeDomain(0, 4));
			}

		}
		if (leftInstanceExpression != null) {
			out.putAll(leftInstanceExpression.getIdentifiers());
		}
		if (rightInstanceExpression != null) {
			out.putAll(rightInstanceExpression.getIdentifiers());
		}
		return out;
	}

	private Identifier getIdentifier(ExpressionVertexType expressionVertexType) {
		Identifier out = null;
		if (expressionVertexType.name().equals("LEFT")) {
			// System.out.println(leftVertex.getIdentifier() + " "
			// + leftAttributeName);
			Identifier identifier = hlclFactory.newIdentifier(volatieLeftElement
					.getInstAttributeFullIdentifier(getSemanticExpression()
							.getLeftAttributeName()), getSemanticExpression()
					.getLeftAttributeName());
			AbstractAttribute attribute = volatieLeftElement.getInstAttribute(
					getSemanticExpression().getLeftAttributeName()).getAttribute();
			if (attribute.getType().equals("Integer")) {
				if (attribute.getDomain() != null)
					identifier.setDomain(attribute.getDomain());
				else
					identifier.setDomain(new RangeDomain(0, 4));
			}
			return identifier;
		}
		if (expressionVertexType.name().equals("RIGHT")) {
			// System.out
			// .println(rightVertex.getIdentifier() + rightAttributeName);
			Identifier identifier = hlclFactory.newIdentifier(volatileRightElement
					.getInstAttributeFullIdentifier(getSemanticExpression()
							.getRightAttributeName()), getSemanticExpression()
					.getRightAttributeName());
			AbstractAttribute attribute = volatileRightElement.getInstAttribute(
					getSemanticExpression().getRightAttributeName()).getAttribute();
			if (attribute.getType().equals("Integer")) {
				if (attribute.getDomain() != null)
					identifier.setDomain(attribute.getDomain());
				else
					identifier.setDomain(new RangeDomain(0, 4));
			}
			return identifier;
		}
		return out;
	}

	private List<Expression> expressionTerms() {
		List<Expression> out = new ArrayList<Expression>();

		List<ExpressionVertexType> expressionVertexTypes = new ArrayList<ExpressionVertexType>();

		ExpressionVertexType left = getSemanticExpression().getLeftExpressionType();
		ExpressionVertexType right = getSemanticExpression()
				.getRightExpressionType();
		if (left != null)
			expressionVertexTypes.add(left);
		if (right != null)
			expressionVertexTypes.add(right);

		for (ExpressionVertexType expressionType : expressionVertexTypes) {
			switch (expressionType) {
			case LEFT:
				out.add(getIdentifier(expressionType));
				break;
			case RIGHT:
				out.add(getIdentifier(expressionType));
				break;
			case LEFTNUMERICEXPRESSIONVALUE:
				out.add(hlclFactory.number(getSemanticExpression().getNumber()));
				break;
			case RIGHTNUMERICEXPRESSIONVALUE:
				out.add(hlclFactory.number(getSemanticExpression().getNumber()));

				break;
			case LEFTVARIABLEVALUE:
				out.add(hlclFactory.number(getVariableIntValue(expressionType)));
				break;
			case RIGHTVARIABLEVALUE:
				out.add(hlclFactory.number(getVariableIntValue(expressionType)));
				break;
			case LEFTSUBEXPRESSION:
				out.add(leftInstanceExpression.createExpression());
				break;
			case RIGHTSUBEXPRESSION:
				out.add(rightInstanceExpression.createExpression());
				break;
			default:
				break;

			}
		}

		return out;
	}

	private int getVariableIntValue(ExpressionVertexType expressionVertexType) {
		String value = null;
		String valueType = null;
		switch (expressionVertexType) {
		case LEFTVARIABLEVALUE:
			value = leftValue;
			valueType = (String) this.volatieLeftElement.getInstAttribute(
					SemanticVariable.VAR_VARIABLETYPE).getValue();
			break;
		case RIGHTVARIABLEVALUE:
			value = rightValue;
			valueType = (String) this.volatileRightElement.getInstAttribute(
					SemanticVariable.VAR_VARIABLETYPE).getValue();

			break;
		default:
		}
		switch (valueType) {
		case "Boolean":
			if (value.equals("true"))
				return 1;
			else
				return 0;
		case "Integer":
			return Integer.parseInt(value);
		case "Enumeration":
			return 0; // TODO
		}
		return 0;
	}

	public InstElement getLeftElement() {
		return volatieLeftElement;
	}

	public void setLeftElement(InstElement leftElement) {
		this.volatieLeftElement = leftElement;
		if (leftElement != null)
			leftElementId = leftElement.getIdentifier();
		else
			leftElementId = null;
	}

	public InstElement getRightElement() {
		return volatileRightElement;
	}

	public void setRightElement(InstElement rightElement) {
		this.volatileRightElement = rightElement;
		if (rightElement != null)
			rightElementId = rightElement.getIdentifier();
		else
			rightElementId = null;
	}

	public InstanceExpression getLeftInstanceExpression() {
		return leftInstanceExpression;
	}

	public void setLeftInstanceExpression(InstanceExpression leftSubExpression) {
		this.leftInstanceExpression = leftSubExpression;
	}

	public InstanceExpression getRightInstanceExpression() {
		return rightInstanceExpression;
	}

	public void setRightInstanceExpression(InstanceExpression rightSubExpression) {
		this.rightInstanceExpression = rightSubExpression;
	}

	public void setLeftInstanceExpression(ExpressionVertexType type,
			SemanticExpressionType semanticExpressionType, String id) {
		if (type == ExpressionVertexType.LEFTSUBEXPRESSION)
			this.leftInstanceExpression = new InstanceExpression(true, id);
		if (type == ExpressionVertexType.LEFTNUMERICEXPRESSIONVALUE)
			this.leftInstanceExpression = new InstanceExpression(true,
					new SemanticExpression(id, semanticExpressionType));
		getSemanticExpression().setLeftExpressionType(type);
	}

	public void setLeftElement(InstElement instElement, String attribute) {
		getSemanticExpression().setLeftExpressionType(ExpressionVertexType.LEFT);
		this.volatieLeftElement = instElement;
		getSemanticExpression().setLeftAttributeName(attribute);
	}

	public void setRightInstanceExpression(ExpressionVertexType type,
			SemanticExpressionType semanticExpressionType, String id) {
		if (type == ExpressionVertexType.RIGHTSUBEXPRESSION)
			this.rightInstanceExpression = new InstanceExpression(true, id);
		if (type == ExpressionVertexType.RIGHTNUMERICEXPRESSIONVALUE)
			this.rightInstanceExpression = new InstanceExpression(true,
					new SemanticExpression(id, semanticExpressionType));
		getSemanticExpression().setRightExpressionType(type);
	}

	public void setRightElement(InstElement instElement, String attribute) {
		getSemanticExpression().setRightExpressionType(ExpressionVertexType.RIGHT);
		this.volatieLeftElement = instElement;
		getSemanticExpression().setRightAttributeName(attribute);
	}

	public int getNumber() {
		return getSemanticExpression().getNumber();
	}

	public void setNumber(int number) {
		getSemanticExpression().setNumber(number);
	}

	public void setSemanticExpressionType(SemanticExpressionType semanticExpressionType) {
		if (customExpression)
			customSemanticExpression.setSemanticExpressionType(semanticExpressionType);
		if (semanticExpressionType == null
				|| semanticExpressionType.isSingleInExpression()) {
			rightInstanceExpression = null;
			volatileRightElement = null;
			getSemanticExpression().setRightExpressionType(null);
		}
	}

	public int getLeftValidExpressions() {
		return getSemanticExpression().getLeftValidExpressions();
	}

	public int getRightValidExpressions() {
		return getSemanticExpression().getRightValidExpressions();
	}

	public int getResultExpressions() {
		return getSemanticExpression().getResultExpressions();
	}

	public SemanticExpression getSemanticExpression() {
		if (customExpression)
			return customSemanticExpression;
		return volatileSemanticExpression;
	}

	public void setSemanticExpression(SemanticExpression semanticExpression) {
		if (customExpression)
			this.customSemanticExpression = semanticExpression;
		else
			this.volatileSemanticExpression = semanticExpression;
	}

	// Required for graph serialization
	public SemanticExpression getCustomSemanticExpression() {
		return customSemanticExpression;
	}

	// Required for graph serialization
	public void setCustomSemanticExpression(SemanticExpression customSemanticExpression) {
		this.customSemanticExpression = customSemanticExpression;
	}

	public boolean isCustomExpression() {
		return customExpression;
	}

	public void setCustomExpression(boolean customExpression) {
		this.customExpression = customExpression;
	}

	public String getSemanticExpressionId() {
		return semanticExpressionId;
	}

	public void setSemanticExpressionId(String semanticExpressionId) {
		this.semanticExpressionId = semanticExpressionId;
	}

	public String getLeftAttributeName() {
		return getSemanticExpression().getLeftAttributeName();
	}

	public String getRightAttributeName() {
		return getSemanticExpression().getRightAttributeName();
	}

	public void setLeftAttributeName(String attribute) {
		getSemanticExpression().setLeftAttributeName(attribute);
	}

	public void setRightAttributeName(String attribute) {
		getSemanticExpression().setRightAttributeName(attribute);
	}

	public String getOperation() {
		return getSemanticExpression().getOperation();
	}

	public ExpressionVertexType getLeftExpressionType() {
		return getSemanticExpression().getLeftExpressionType();
	}

	public ExpressionVertexType getRightExpressionType() {
		return getSemanticExpression().getRightExpressionType();
	}

	public void setLeftExpressionType(ExpressionVertexType expressionVertexType) {
		getSemanticExpression().setLeftExpressionType(expressionVertexType);
	}

	public void setRightExpressionType(ExpressionVertexType expressionVertexType) {
		getSemanticExpression().setRightExpressionType(expressionVertexType);
	}

	public String getSideElementIdentifier(
			ExpressionVertexType expressionVertexType) {
		if (expressionVertexType == ExpressionVertexType.LEFT) {
			if (getLeftElement() != null)
				return getLeftElement().getIdentifier();
			else
				return leftElementId;
		} else if (expressionVertexType == ExpressionVertexType.RIGHT) {
			if (getRightElement() != null)
				return getRightElement().getIdentifier();
			else
				return rightElementId;
		}
		return null;
	}

	public String getRightElementId() {
		return rightElementId;
	}

	public void setRightElementId(String rightElementId) {
		this.rightElementId = rightElementId;
	}

	public String getLeftElementId() {
		return leftElementId;
	}

	public void setLeftElementId(String leftElementId) {
		this.leftElementId = leftElementId;
	}

	public String getLeftValue() {
		return leftValue;
	}

	public void setLeftValue(String leftValue) {
		this.leftValue = leftValue;
	}

	public String getRightValue() {
		return rightValue;
	}

	public void setRightValue(String rightValue) {
		this.rightValue = rightValue;
	}

	public String getElementAttributeIdentifier(
			ExpressionVertexType expressionVertexType) {
		switch (expressionVertexType) {
		case LEFT:
			if (getLeftElement() != null)
				return getLeftElement().getIdentifier() + "_"
						+ getLeftAttributeName();
			break;
		case RIGHT:
			if (getRightElement() != null)
				return getRightElement().getIdentifier() + "_"
						+ getRightAttributeName();
			break;
		case LEFTVARIABLEVALUE:
			if (getLeftElement() != null)
				return getLeftElement().getIdentifier() + "_" + leftValue;
			break;
		case RIGHTVARIABLEVALUE:
			if (getRightElement() != null)
				return getRightElement().getIdentifier() + "_" + rightValue;
			break;
		default:
			return null;
		}
		return null;
	}

	public InstElement getSideElement(ExpressionVertexType expressionVertexType) {
		switch (expressionVertexType) {
		case LEFT:
		case LEFTVARIABLEVALUE:
			return getLeftElement();
		case RIGHT:
		case RIGHTVARIABLEVALUE:
			return getRightElement();
		default:
		}
		return null;
	}

	public void setElement(InstVertex vertex,
			ExpressionVertexType expressionVertexType) {
		switch (expressionVertexType) {
		case LEFT:
		case LEFTVARIABLEVALUE:
			this.setLeftElement(vertex);
			break;
		case RIGHT:
		case RIGHTVARIABLEVALUE:
			this.setRightElement(vertex);
		default:
		}
	}

	public void setAttributeName(String attributeName,
			ExpressionVertexType expressionVertexType) {
		switch (expressionVertexType) {
		case LEFT:
			this.setLeftAttributeName(attributeName);
			break;
		case RIGHT:
			this.setRightAttributeName(attributeName);
			break;
		case LEFTVARIABLEVALUE:
			this.leftValue = attributeName;
			break;
		case RIGHTVARIABLEVALUE:
			this.rightValue = attributeName;
		default:
		}
	}

	public boolean isSingleInExpression() {
		return getSemanticExpression().isSingleInExpression();
	}

	public String toString() {
		return "";// expressionStructure();
	}

	public String expressionStructure() {
		String out = "";
		int i = 0;
		for (ExpressionVertexType expressionVertex : getSemanticExpression()
				.getExpressionTypes()) {
			// if (expressionConnectors.size() > i)
			// out += " " + expressionConnectors.get(i) + " ";
			switch (expressionVertex) {
			case LEFTVARIABLEVALUE:
				out += leftValue;
				break;
			case RIGHTVARIABLEVALUE:
				out += rightValue;
				break;
			case LEFT:
				out += getLeftAttributeName();
				break;
			case RIGHT:
				out += getRightAttributeName();

				break;
			case LEFTSUBEXPRESSION:
				out += "(" + leftInstanceExpression.expressionStructure() + ")";
				break;
			case RIGHTSUBEXPRESSION:
				out += "(" + rightInstanceExpression.expressionStructure()
						+ ")";
				break;
			default:
				break;

			}
			i++;
		}
		return out;
	}
}
