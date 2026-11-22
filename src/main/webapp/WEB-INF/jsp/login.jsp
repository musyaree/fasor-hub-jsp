<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Login - Fasor Hub</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200&display=swap" rel="stylesheet">
  <style>
    :root {
      --color-brand: #1F5FBF;
      --color-brand-fg: #FFFFFF;
      --color-brand-tint: #E6EEFA;
      --color-bg: #F4F7FB;
      --color-surface: #FFFFFF;
      --color-ink: #142033;
      --color-ink-deep: #0B1426;
      --color-text-muted: #4A5A70;
      --color-border: #7A8AA2;
      --color-hairline: #D5DDE8;
      --color-line-on-ink: #8DB4F0;
      --color-text-on-ink: #A9B6C9;
      --color-status-occupied: #C62F2F;
      --color-status-available: #1E8E3E;
      --radius-md: 8px;
      --radius-lg: 12px;
      --shadow-elevated: 0 4px 16px rgb(20 32 51 / 0.14);
    }

    * { box-sizing: border-box; }

    html, body {
      margin: 0;
      min-height: 100vh;
      font-family: 'IBM Plex Sans', system-ui, sans-serif;
      font-size: 16px;
      line-height: 1.5;
      color: var(--color-ink);
      background: var(--color-bg);
    }

    .auth-layout {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }

    @media (min-width: 800px) {
      .auth-layout {
        flex-direction: row;
      }
    }

    .auth-brand {
      background: var(--color-ink-deep);
      color: #fff;
      padding: 32px 24px;
      text-align: center;
    }

    @media (min-width: 800px) {
      .auth-brand {
        width: 40%;
        min-height: 100vh;
        display: flex;
        flex-direction: column;
        justify-content: center;
        padding: 48px;
        text-align: left;
      }
    }

    .auth-brand__title {
      font-size: 30px;
      font-weight: 700;
      line-height: 1.2;
      margin: 0;
    }

    @media (min-width: 800px) {
      .auth-brand__title {
        font-size: 48px;
      }
    }

    .auth-brand__subtitle {
      font-size: 14px;
      color: var(--color-text-on-ink);
      margin-top: 8px;
    }

    .auth-form-container {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 32px 16px;
    }

    @media (min-width: 800px) {
      .auth-form-container {
        padding: 48px;
      }
    }

    .auth-card {
      width: 100%;
      max-width: 400px;
      background: var(--color-surface);
      border-radius: var(--radius-lg);
      padding: 32px;
      box-shadow: var(--shadow-elevated);
    }

    .auth-card__title {
      font-size: 24px;
      font-weight: 700;
      margin: 0 0 24px;
      text-align: center;
    }

    .form-group {
      margin-bottom: 16px;
    }

    .form-label {
      display: block;
      font-size: 14px;
      font-weight: 500;
      margin-bottom: 6px;
    }

    .form-input {
      display: block;
      width: 100%;
      height: 44px;
      padding: 0 12px;
      font-size: 14px;
      color: var(--color-ink);
      background: var(--color-surface);
      border: 1px solid var(--color-border);
      border-radius: var(--radius-md);
    }

    .form-input:focus {
      outline: none;
      border-color: var(--color-brand);
      box-shadow: 0 0 0 3px var(--color-brand-tint);
    }

    .error-message {
      background: #FBE0E0;
      color: #7A1414;
      padding: 12px 16px;
      border-radius: var(--radius-md);
      font-size: 14px;
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .success-message {
      background: #DDF3E4;
      color: #0B4F22;
      padding: 12px 16px;
      border-radius: var(--radius-md);
      font-size: 14px;
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .btn {
      display: block;
      width: 100%;
      height: 44px;
      font-size: 14px;
      font-weight: 600;
      border: none;
      border-radius: var(--radius-md);
      cursor: pointer;
      margin-top: 24px;
    }

    .btn--primary {
      background: var(--color-brand);
      color: var(--color-brand-fg);
    }

    .btn--primary:hover {
      background: #1a4fa3;
    }

    .btn--primary:focus {
      outline: 2px solid var(--color-brand);
      outline-offset: 2px;
    }

    .auth-footer {
      text-align: center;
      margin-top: 24px;
      font-size: 14px;
      color: var(--color-text-muted);
    }

    .auth-footer a {
      color: var(--color-brand);
      text-decoration: none;
      font-weight: 500;
    }

    .auth-footer a:hover {
      text-decoration: underline;
    }

    .material-symbols-outlined {
      font-family: 'Material Symbols Outlined';
      font-size: 20px;
      vertical-align: middle;
    }
  </style>
</head>
<body>
  <div class="auth-layout">
    <div class="auth-brand">
      <h1 class="auth-brand__title">Fasor Hub</h1>
      <p class="auth-brand__subtitle">Sports Facility Booking</p>
    </div>

    <div class="auth-form-container">
      <div class="auth-card">
        <h2 class="auth-card__title">Login</h2>

        <c:if test="${not empty flashSuccess}">
          <div class="success-message">
            <span class="material-symbols-outlined">check_circle</span>
            ${flashSuccess}
          </div>
        </c:if>

        <c:if test="${not empty error}">
          <div class="error-message" role="alert">
            <span class="material-symbols-outlined">cancel</span>
            ${error}
          </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/login">
          <input type="hidden" name="_csrf" value="${sessionScope._csrf}">

          <div class="form-group">
            <label class="form-label" for="email">Email</label>
            <input type="email" id="email" name="email" class="form-input"
                   value="${email}" required autofocus>
          </div>

          <div class="form-group">
            <label class="form-label" for="password">Password</label>
            <input type="password" id="password" name="password" class="form-input" required>
          </div>

          <button type="submit" class="btn btn--primary">Login</button>
        </form>

        <div class="auth-footer">
          Don't have an account? <a href="${pageContext.request.contextPath}/register">Register</a>
        </div>
      </div>
    </div>
  </div>
</body>
</html>
