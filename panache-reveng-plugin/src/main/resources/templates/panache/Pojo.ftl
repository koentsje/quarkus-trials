${pojo.getPackageDeclaration()}
// Generated ${date} by Hibernate Tools ${version}

<#assign classbody>
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

<#include "PojoTypeDeclaration.ftl"/> {

<#if !pojo.isInterface()>
<#include "PojoFields.ftl"/>

<#include "PojoConstructors.ftl"/>
<#--
<#include "PojoPropertyAccessors.ftl"/>
-->
<#include "PojoToString.ftl"/>

<#include "PojoEqualsHashcode.ftl"/>

<#else>
<#--
<#include "PojoInterfacePropertyAccessors.ftl"/>
-->
</#if>
<#include "PojoExtraClassCode.ftl"/>

}
</#assign>

${pojo.generateImports()}
${classbody}

