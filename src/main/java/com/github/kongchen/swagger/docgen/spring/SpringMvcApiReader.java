package com.github.kongchen.swagger.docgen.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import scala.Option;
import scala.Predef;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.JavaConverters;
import scala.collection.mutable.LinkedHashMap;

import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import com.google.common.base.CharMatcher;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.core.ApiValues;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.model.AllowableListValues;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.Authorization;
import com.wordnik.swagger.model.Model;
import com.wordnik.swagger.model.ModelProperty;
import com.wordnik.swagger.model.ModelRef;
import com.wordnik.swagger.model.Operation;
import com.wordnik.swagger.model.Parameter;
import com.wordnik.swagger.model.ResponseMessage;
/**
 * 
 * @author tedleman
 * 
 * The use-goal of this object is to return an ApiListing object from the read() method. 
 * The listing object is populated with other api objects, contained by ApiDescriptions
 * 
 * Generation Order: 
 * 
 * ApiListing ==> ApiDescriptions ==> Operations ==> Parameters
 *												 ==> ResponseMessages
 *
 * Models are generated as they are detected and ModelReferences are added for each
 */
public class SpringMvcApiReader  {
	private ApiSource apiSource;
	private ApiListing apiListing;
	private String resourcePath;
	private List<String> produces;
	private List<String> consumes;
	private Map<String,Model> models;
	
	private static final Option<String> DEFAULT_OPTION = Option.apply(""); //<--comply with scala option to prevent nulls
	private static final String[] RESERVED_PACKAGES = {"java","org.springframework"};


	public SpringMvcApiReader(ApiSource aSource){
		apiSource=aSource;
		apiListing = null;
		resourcePath="";
		models = new HashMap<String,Model>();
		produces = new ArrayList<String>();
		consumes = new ArrayList<String>();
	}
	
	/**
	 * 
	 * @param basePath
	 * @param c
	 * @param swaggerConfig
	 * @param methods
	 * 
	 * Run through annotations in approved controller class to generate ApiListing
	 * This method is called from the document source class and calls the generating methods
	 * to populate an ApiListing with all proper resources
	 */
	public ApiListing read(SpringResource resource, SwaggerConfig swaggerConfig){
		List<Method> methods = resource.getMethods();
		List<String> protocols = new ArrayList<String>();
		List<ApiDescription> apiDescriptions = new ArrayList<ApiDescription>();
		List<Authorization> authorizations = Collections.emptyList();//TODO
		String newBasePath="";
		String description="";
		
		// Add the description from the controller api
		Class<?> controller = resource.getControllerClass();
		if( controller != null && controller.isAnnotationPresent(Api.class)) {
		  Api api = (Api) controller.getAnnotation(Api.class);
		  description = api.description();
		}

		resourcePath = resource.getControllerMapping();
		newBasePath=generateBasePath(apiSource.getBasePath(),resourcePath); 

		for(Method m: methods){
			if(m.isAnnotationPresent(RequestMapping.class)){
				apiDescriptions.add(generateApiDescription(m));
			}
		}

		apiListing = new ApiListing(swaggerConfig.apiVersion(), swaggerConfig.getSwaggerVersion(),newBasePath, resourcePath, 
				scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(produces.iterator())), 
				scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(consumes.iterator())), 
				scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(protocols.iterator())), 
				scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(authorizations.iterator())), 
				scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(apiDescriptions.iterator())),
				generateModels(models),Option.apply(description), 0);
		return apiListing;
	}
	
	//--------Swagger Resource Generators--------//

	
	private String generateBasePath(String bPath, String rPath){
		String domain = "";

		//check for two character domain at beginning of resourcePath
		if(rPath.charAt(2)=='/'){
			domain = rPath.substring(0,2);
			this.resourcePath =rPath.substring(2);
		}else if(rPath.charAt(3)=='/'){
			domain = rPath.substring(1,3);
			this.resourcePath =rPath.substring(3);
		}

		//check for first & trailing backslash 
		if(bPath.lastIndexOf('/')!=(bPath.length()-1) && StringUtils.isNotEmpty(domain)){
			bPath = bPath+'/';
		}
		
		//TODO this should be done elsewhere
		if(this.resourcePath.charAt(0)!='/'){
			this.resourcePath = '/'+this.resourcePath;
		}

		return bPath+domain;
	}
	
	private String generateFullPath(String path){
		if(StringUtils.isNotEmpty(path)){
			return this.resourcePath + (path.startsWith("/") ? path : '/' + path);
		} else {
			return this.resourcePath;
		}
	}
	
	/**
	 * Creates the ApiDescription object that holds all Operations
	 * @param Method m
	 * @return ApiDescription
	 */
	private ApiDescription generateApiDescription(Method m){
		List<Operation> operations = new ArrayList<Operation>();
		RequestMapping requestMapping = (RequestMapping) m.getAnnotation(RequestMapping.class);
		String path="";
		if(requestMapping.value()!=null&&requestMapping.value().length!=0){
			path = generateFullPath(requestMapping.value()[0]);
		}else{
			path=resourcePath;
		}
		if(m.isAnnotationPresent(RequestMapping.class)){
			operations.add(generateOperation(m));
		}
		return new ApiDescription(path,DEFAULT_OPTION,
				scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(operations.iterator())), false);	
	}

	/**
	 * Generates operations for the ApiDescription
	 * @param Method m
	 * @return Operation
	 */
	private Operation generateOperation(Method m){
		Class<?> clazz;
		ApiOperation apiOperation;
		RequestMapping requestMapping; 
		ResponseBody responseBody; 
		ResponseStatus responseStatus;
		String responseBodyName = "";
		String method = "";
		String description = "";
		String notes = "";
		List<String> opProduces = new ArrayList<String>();
		List<String> opConsumes = new ArrayList<String>();
		List<Parameter> parameters = new ArrayList<Parameter>();
		List<ResponseMessage> responseMessages = new ArrayList<ResponseMessage>();

		apiOperation = (ApiOperation) m.getAnnotation(ApiOperation.class);
		requestMapping = (RequestMapping) m.getAnnotation(RequestMapping.class);
		responseBody = (ResponseBody) m.getAnnotation(ResponseBody.class);
		responseStatus = (ResponseStatus) m.getAnnotation(ResponseStatus.class);


		if(m.getReturnType().equals(ResponseEntity.class)){
			clazz = getGenericSubtype(m.getReturnType(),m.getGenericReturnType());
		}else{
			clazz = m.getReturnType();
		}
		
		clazz = getGenericSubtype(m.getReturnType(),m.getGenericReturnType());

		if(requestMapping.produces()!=null){
			opProduces = Arrays.asList(requestMapping.produces());
			for(String str:opProduces){
				if(!produces.contains(str)){
					produces.add(str);
				}
			}
		}
		if(requestMapping.consumes()!=null){
			opConsumes = Arrays.asList(requestMapping.consumes());
			for(String str:opConsumes){
				if(!consumes.contains(str)){
					consumes.add(str);
				}
			}
		}

		if(apiOperation!=null){
			description = apiOperation.value();
			notes = apiOperation.notes();
		}
		if(responseStatus!=null){
			responseMessages = generateResponseMessages(m);
		}
		if(responseBody!=null){
			responseBodyName=(clazz.getSimpleName());
			addToModels(clazz);
		}
		if(requestMapping.method()!=null&&requestMapping.method().length!=0){
			method = requestMapping.method()[0].toString();
		}
		if(m.getParameterTypes()!=null){
			parameters = generateParameters(m);
		}
		if(notes!=""){
			notes = "<p>"+notes+"</p>";
		}
		
		return new Operation(method, 
				description, notes, responseBodyName, m.getName(), 0, 
				scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(opProduces.iterator())), 
				scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(opConsumes.iterator())), 
				null, null, 
				scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(parameters.iterator())), 
				scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(responseMessages.iterator())),
				DEFAULT_OPTION);
	}

	/**
	 * Generates parameters for each Operation
	 * @param Method m
	 * @return List<Parameter>
	 */
	private List<Parameter> generateParameters(Method m){
		Annotation[][] annotations = m.getParameterAnnotations();
		List<Parameter> params = new ArrayList<Parameter>();
		for(int i=0; i<annotations.length;i++){ //loops through parameters
			AllowableListValues allowed = null;
			String dataType="";
			String type = "";
			String name = "";
			boolean required=true;
			Annotation[] anns = annotations[i];
			Class<?> clazz = m.getParameterTypes()[i];
			String description = "";
			List<String> allowableValuesList;
			
			for(int x=0;x<anns.length;x++){ //loops through annotations for each parameter
				if(anns[x].annotationType().equals(PathVariable.class)){ 
					PathVariable pathVariable = (PathVariable) anns[x];
					name=pathVariable.value();
					type = ApiValues.TYPE_PATH();
				}else if(anns[x].annotationType().equals(RequestBody.class)){
					RequestBody requestBody = (RequestBody) anns[x];
					type = ApiValues.TYPE_BODY();
					name = ApiValues.TYPE_BODY();
					required = requestBody.required();
				}else if(anns[x].annotationType().equals(RequestParam.class)){
					RequestParam requestParam = (RequestParam) anns[x];
					name = requestParam.value();
					type = ApiValues.TYPE_QUERY();
					required = requestParam.required();
				}else if(anns[x].annotationType().equals(ApiParam.class)){
					try{
						ApiParam apiParam = (ApiParam) anns[x];
						if(apiParam.value()!=null)
							description = apiParam.value();
						if(apiParam.allowableValues()!=null){
							
							String allowableValues = CharMatcher.anyOf("[] ").removeFrom(apiParam.allowableValues());
							if(!(allowableValues.equals(""))){
								allowableValuesList = Arrays.asList(allowableValues.split(","));
								allowed = new AllowableListValues(
										scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(allowableValuesList.iterator())),"LIST");
							}
							
						}
					}catch(Exception e){}
				}
			}

			if(clazz.isArray()){
				String clazzName = CharMatcher.anyOf("[]").removeFrom(clazz.getSimpleName());
				clazzName = generateTypeString(clazzName);
				dataType = "Array["+clazzName+"]";
				addToModels(clazz);
			}else{
				dataType = generateTypeString(clazz.getSimpleName());
				addToModels(clazz);
			}
			
			if(description.length()!=0){
				description = "<p>"+description+"</p>";
			}
			params.add(new Parameter(name, Option.apply(description), DEFAULT_OPTION, required, false, dataType, 
					allowed, type, DEFAULT_OPTION));
		}
		return params;
	}
	
	
	/**
	 * Generates response messages for each Operation
	 * @param Method m
	 * @return List<ResponseMessage>
	 */
	private List<ResponseMessage> generateResponseMessages(Method m){
		ResponseStatus responseStatus = m.getAnnotation(ResponseStatus.class);
		List<ResponseMessage> responseMessages = new ArrayList<ResponseMessage>();
		responseMessages.add(new ResponseMessage(responseStatus.value().value(),responseStatus.value().name(),DEFAULT_OPTION));
		return responseMessages;
	}

	/**
	 * Generates a Model object for a Java class. Takes properties from either fields or methods.
	 * Recursion occurs if a ModelProperty needs to be modeled
	 * @param Class<?> clazz
	 * @return Model
	 */
	private Model generateModel(Class<?> clazz){ 
		ApiModel apiModel;
		ModelRef modelRef = null;
		String modelDescription = "";
		LinkedHashMap<String, ModelProperty> modelProps = new LinkedHashMap<String,ModelProperty>();
		List<String> subTypes = new ArrayList<String>();

		if(clazz.isAnnotationPresent(ApiModel.class)){
			apiModel = clazz.getAnnotation(ApiModel.class);
			if(apiModel.description()!=null){
				modelDescription = apiModel.description();
			}
		}

		//<--Model properties from fields-->
		int x= 0;
		for(Field field:clazz.getDeclaredFields()){
			//Only use fields if they are annotated - otherwise use methods
			XmlElement xmlElement;
			ApiModelProperty amp;
			Class<?> c;
			String name;
			boolean required=false;
			String description = "";
			if(!(field.getType().equals(clazz))){
				//if the types are the same, model will already be generated
				modelRef = generateModelRef(clazz,field); //recursive IFF there is a generic sub-type to be modeled
			}
			if(field.isAnnotationPresent(XmlElement.class)||
					field.isAnnotationPresent(ApiModelProperty.class)){ 
				if(field.getAnnotation(XmlElement.class)!=null){
					xmlElement = field.getAnnotation(XmlElement.class);
					required = xmlElement.required();
				}
				if(field.getAnnotation(ApiModelProperty.class)!=null){
					amp = field.getAnnotation(ApiModelProperty.class);
					if(required!=true){ //if required has already been changed to true, it was noted in XmlElement
						required = amp.required();
					}
					description = amp.value();
				}
				
				if(!(field.getType().equals(clazz))){
					c = field.getType();
					name = field.getName();
					addToModels(c);
					subTypes.add(c.getSimpleName());
					modelProps.put(name, generateModelProperty(c,x,required,modelRef,description));
					x++;
				}
			}
		}
		
		//<--Model properties from methods-->
		int i=0;
		for(Method m: clazz.getMethods()){
			boolean required=false;
			String description = "";
			ApiModelProperty amp;
			//look for required field in XmlElement annotation
			
			if(!(m.getReturnType().equals(clazz))){
				modelRef = generateModelRef(clazz,m); //recursive IFF there is a generic sub-type to be modeled
			}
			
			if(m.isAnnotationPresent(XmlElement.class)){
				XmlElement xmlElement = m.getAnnotation(XmlElement.class);
				required=xmlElement.required();
			}else if(m.isAnnotationPresent(JsonIgnore.class)
					||m.isAnnotationPresent(XmlTransient.class)){
				continue; //ignored fields
			}

			if(m.getAnnotation(ApiModelProperty.class)!=null){
				amp = m.getAnnotation(ApiModelProperty.class);
				required = amp.required();
				description = amp.value();
			}

			//get model properties from methods
			if((m.getName().startsWith("get")||m.getName().startsWith("is"))
					&&!(m.getName().equals("getClass"))){ 
				Class<?> c = m.getReturnType();
				String name="";
				try{
					if(m.getName().startsWith("get")){
						name = m.getName().substring(3);
					}else{
						name = m.getName().substring(2);
					}
					String firstLetter = name.substring(0,1).toLowerCase(); //convert to camel case
					name = firstLetter+name.substring(1);
				}catch(Exception e){}
				if(!(m.getReturnType().equals(clazz))){
					addToModels(c); //recursive 
				}
				if(!modelProps.contains(name)){
					modelProps.put(name, generateModelProperty(c,i,required,modelRef,description));
				}

			}
			i++;
		}
		if(modelDescription.length()!=0){
			modelDescription = "<p>"+modelDescription+"</p>";
		}
		
		return new Model(clazz.getSimpleName(), "", "", modelProps, 
				Option.apply(modelDescription), DEFAULT_OPTION, DEFAULT_OPTION, 
				scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(subTypes.iterator())));
	}

	/**
	 * Generates a ModelProperty for given model class. Supports String enumerations only.
	 * @param Class<?> clazz
	 * @param int position
	 * @param boolean required
	 * @param ModelRef modelRef
	 * @param String description
	 * @return ModelProperty
	 */
	private ModelProperty generateModelProperty(Class<?> clazz, int position, boolean required, ModelRef modelRef, 
			String description){
		AllowableListValues allowed = null;
		String name=clazz.getSimpleName();

		if(!(isModel(clazz))){ 
			name=name.toLowerCase();
		}
		//check for enumerated values - currently strings only
		//TODO: support ranges
		if(clazz.isEnum()){
			List<String> enums = new ArrayList<String>();
			for(Object obj: clazz.getEnumConstants()){
				enums.add(obj.toString());
			}
			allowed = new AllowableListValues(
					scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(enums.iterator())),"LIST");
		}
		
		return new ModelProperty(name,
				"",position, required, Option.apply(description), allowed, 
				Option.apply(modelRef));
	}
	
	/**
	 * Generates a model reference based on a method
	 * @param Class<?> clazz
	 * @param Method m
	 * @return ModelRef
	 */
	private ModelRef generateModelRef(Class<?> clazz, Method m){
		ModelRef modelRef = null; //can be null
		if(Collection.class.isAssignableFrom(m.getReturnType())||m.getReturnType().equals(ResponseEntity.class)
				||m.getReturnType().equals(JAXBElement.class)){
			Class<?> c = getGenericSubtype(m.getReturnType(), m.getGenericReturnType());
			if(isModel(c)&&!(c.equals(clazz))){
				addToModels(c);
				modelRef = new ModelRef(c.getSimpleName(), Option.apply(c.getSimpleName()),
						Option.apply(c.getSimpleName())); 
			}else{
				modelRef = new ModelRef(c.getSimpleName().toLowerCase(),DEFAULT_OPTION,DEFAULT_OPTION);
			}
		}
		return modelRef;
	}
	
	/**
	 * Generates a model reference based on a field
	 * @param Class<?> clazz
	 * @param Field f
	 * @return ModelRef
	 */
	private ModelRef generateModelRef(Class<?> clazz, Field f){
		ModelRef modelRef = null;
		if(Collection.class.isAssignableFrom(f.getType())||f.getType().equals(ResponseEntity.class)
				||f.getType().equals(JAXBElement.class)){
			Class<?> c = getGenericSubtype(f.getType(), f.getGenericType());
			if(isModel(c)&&!(c.equals(clazz))){
				addToModels(c);
				modelRef = new ModelRef(c.getSimpleName(), Option.apply(c.getSimpleName()),
						Option.apply(c.getSimpleName())); 
			}else{
				modelRef = new ModelRef(c.getSimpleName().toLowerCase(),DEFAULT_OPTION,DEFAULT_OPTION);
			}
		}
		return modelRef;
	}

	//-------------Helper Methods------------//
	
	private Class<?> getGenericSubtype(Class<?>clazz, Type t){
		if(!(clazz.getName().equals("void")||t.toString().equals("void"))){
			try{
				ParameterizedType paramType = (ParameterizedType) t;
				Type[] argTypes = paramType.getActualTypeArguments();
				if(argTypes.length>0){
					Class<?> c = (Class<?>) argTypes[0];
					return c;
				}
			}catch(ClassCastException e){
				//FIXME: find out why this happens to only certain types
			}
		}
		return clazz;
	}

	private void addToModels(Class<?> clazz){
		if(isModel(clazz)&&!(models.containsKey(clazz.getSimpleName()))){
			models.put(clazz.getSimpleName(),generateModel(clazz)); 
		}
	}
	
	private boolean isModel(Class<?> clazz){
		try{
			for(String str: RESERVED_PACKAGES){
				if(clazz.getPackage().getName().contains(str)){
					return false;
				}
			}
			if(SwaggerSpec.baseTypes().contains(clazz.getSimpleName().toLowerCase())){
				return false;
			}else{
				return true;
			}
		}catch(NullPointerException e){ //null pointer for package names - wouldn't model something without a package. skip
			return false;
		}
	}

	private String generateTypeString(String clazzName){
		String typeString = clazzName;
		if(SwaggerSpec.baseTypes().contains(clazzName.toLowerCase())){
			typeString = clazzName.toLowerCase();
		}
		return typeString;
	}

	private Option<scala.collection.immutable.Map<String, Model>> generateModels(Map<String,Model> javaModelMap){
		Option<scala.collection.immutable.Map<String, Model>> models = Option.apply(toScalaMap(javaModelMap));
		return models; 
	}

	private static <A, B> scala.collection.immutable.Map<String, Model> toScalaMap(Map<String, Model> map) {
		return JavaConverters.mapAsScalaMapConverter(map).asScala().toMap(Predef.<Tuple2<String, Model>>conforms());
	}


























}
