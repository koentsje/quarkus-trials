<#-- <#include "Ejb3FieldGetAnnotation.ftl"/> -->
<#if pojo.hasIdentifierProperty()>
<#if field.name.equals(clazz.identifierProperty.name)>
${pojo.generateAnnIdGenerator()}
</#if>
<#if c2h.isOneToOne(field)>
${pojo.generateOneToOneAnnotation(field, md)}
<#elseif c2h.isManyToOne(field)>
${pojo.generateManyToOneAnnotation(field)}
<#--TODO support optional and targetEntity-->
${pojo.generateJoinColumnsAnnotation(field, md)}
<#elseif c2h.isCollection(field)>
${pojo.generateCollectionAnnotation(field, md)}
<#else>
${pojo.generateBasicAnnotation(field)}
${pojo.generateAnnColumnAnnotation(field)}
</#if>
</#if>