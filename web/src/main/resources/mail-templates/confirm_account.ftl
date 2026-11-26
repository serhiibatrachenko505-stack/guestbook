<#include "layout/header.ftl">

<h2 style="margin-top:0; color:#2c3e50; font-size:20px;">Підтвердження реєстрації</h2>

<p style="margin:6px 0;">Вітаємо, <b>${username}</b>!</p>
<p style="margin:6px 0;">Ви зареєструвалися в каталозі книг. Щоб активувати акаунт, натисніть кнопку нижче.</p>

<p style="margin:25px 0;">
    <a href="${link}"
       style="background:#4CAF50; color:#ffffff; padding:10px 15px; text-decoration:none; border-radius:5px;">
        Підтвердити акаунт
    </a>
</p>

<p style="font-size:13px; color:#7f8c8d;">
    Якщо кнопка не працює, скопіюйте посилання в браузер:<br>
    <span style="word-break:break-all;">${link}</span>
</p>
<p style="font-size:13px; color:#7f8c8d;">Якщо ви не реєструвалися, просто проігноруйте цей лист.</p>

<#include "layout/footer.ftl">
