<#-- // Fields -->
<#foreach field in pojo.getAllPropertiesIterator()><#if pojo.getMetaAttribAsBool(field, "gen-property", true)> <#if pojo.hasMetaAttribute(field, "field-description")>    /**
     ${pojo.getFieldJavaDoc(field, 0)}
     */
 </#if>
    <#include "GetFieldAnnotation.ftl"/>
    public ${pojo.getJavaTypeName(field, true)} ${c2j.keyWordCheck(field.name)}<#if pojo.hasFieldInitializor(field, true)> = ${pojo.getFieldInitialization(field, true)}</#if>;
</#if>
</#foreach>