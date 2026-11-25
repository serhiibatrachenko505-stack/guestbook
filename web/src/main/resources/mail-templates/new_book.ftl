<#include "layout/header.ftl">

<h2 style="margin-top:0; color:#2c3e50; font-size:20px;">Нова книга додана до каталогу</h2>

<div style="background:#f9fbfd; border:1px solid #d6e4f0; border-left:5px solid #3498db; border-radius:6px; padding:15px 20px; margin-bottom:20px;">
    <p style="margin:6px 0;">Назва: <b>${title}</b></p>
    <p style="margin:6px 0;">Автор: <b>${author}</b></p>
    <p style="margin:6px 0;">Рік видання: <b>${year!"—"}</b></p>
</div>

<#if year??>
    <#assign yearClean = year?string?replace("[^0-9]", "", "r")>
    <#if yearClean?has_content && yearClean?number < 2000>
        <p style="background:#fff4e0; border:1px solid #f0c36d; color:#a36b00; padding:10px 15px; border-radius:6px;">
            <b>Раритетне видання!</b>
        </p>
    </#if>
</#if>

<#if id??>
    <p style="margin:25px 0;">
        <a href="http://localhost:8080/books/${id?c}"
           style="background:#4CAF50; color:#ffffff; padding:10px 15px; text-decoration:none; border-radius:5px;">
            Переглянути книгу
        </a>
    </p>
</#if>

<p style="font-size:13px; color:#7f8c8d;">Дата додавання: ${added?string("yyyy-MM-dd HH:mm")}</p>

<#include "layout/footer.ftl">