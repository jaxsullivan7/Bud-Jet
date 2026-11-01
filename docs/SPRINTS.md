# Registro de sprints · Bud Jet

> Histórico de ejecución reconstruido en Jira con un equipo simulado de cinco personas. Las fechas de sprint y los worklogs siguen el calendario de entrega; la creación de incidencias se centralizó al cierre del proyecto.

## Equipo

| Persona | Rol | Etiqueta Jira | Capacidad |
|---|---|---|---|
| Andrea Vidal | Frontend | `fe-andrea` | 8 h/día |
| Pablo Ferrer | Frontend | `fe-pablo` | 8 h/día |
| Marta Ruiz | Backend | `be-marta` | 8 h/día |
| Diego Salas | Backend | `be-diego` | 8 h/día |
| Lucía Moreno | QA | `qa-lucia` | 8 h/día |

Jira solo dispone de una cuenta real. Para que el histórico sea trazable, cada incidencia lleva la etiqueta de la persona y el rol en el título; los worklogs se registran bajo la cuenta de administración y el comentario indica quién trabajó. Hay filtros guardados por persona, además de `Bud Jet · Ausencias` y `Bud Jet · Impedimentos y bugs`.

## Calendario

| Sprint | Fechas | Objetivo | Capacidad | Planificado | Registrado | Puntos | Estado |
|---|---|---|---:|---:|---:|---:|---|
| Sprint 0 · Inception | 7–11 sep 2026 | Arquitectura, sistema visual y backlog | 200 h | 108 h | 108 h | 27 | Cerrado |
| Sprint 1 · Fundaciones y v0.1 | 14–25 sep 2026 | Primera versión usable | 392 h | 308 h | 302 h | 72 | Cerrado |
| Sprint 2 · v0.2 y v0.3 | 28 sep–9 oct 2026 | Recurrentes, objetivos, backup y biometría | 392 h | 262 h | 262 h | 61 | Cerrado |
| Sprint 3 · v0.4 y v0.5 | 12–23 oct 2026 | Saldo inicial, paletas, programados y análisis | 376 h | 264 h | 264 h | 59 | Cerrado |
| Sprint 4 · Cartera y release | 26 oct–6 nov 2026 | Cartera, calculadora y entrega v0.6.0 | 392 h | 288 h* | 227 h | 65 | Activo |
| **Total** | | | **1.752 h** | **1.230 h** | **1.163 h** | **284** | |

\* Sprint 4 incluye 24 h de UAT que siguen abiertas al cierre de este registro.

## Ausencias

| Sprint | Persona | Motivo | Impacto |
|---|---|---|---|
| Sprint 1 | Pablo Ferrer | Formación interna de Compose | −8 h |
| Sprint 2 | Marta Ruiz | Baja médica | −8 h |
| Sprint 3 | Diego Salas | Vacaciones (2 días) | −16 h |
| Sprint 3 | Lucía Moreno | Cita médica | −8 h |
| Sprint 4 | Andrea Vidal | Vacaciones (1 día) | −8 h |

## Retrasos e imprevistos

| Sprint | Incidencia | Impacto |
|---|---|---|
| Sprint 2 | El emulador de QA dejó de arrancar tras actualizar AGP | −1 día de validación; 8 h no planificadas |
| Sprint 3 | La migración v3→v4 se subestimó | +1,5 días; el release pasó del miércoles al viernes |
| Sprint 3 | Los importes se redondeaban a euros enteros | 8 h de corrección y regresión |
| Sprint 3 | Un ingreso futuro afectaba al saldo actual | 8 h de corrección en v0.4.1 |
| Sprint 4 | El release se bloqueó por lint y emulador | +1 día; 8 h no planificadas |
| Sprint 4 | Retirada superior al valor de la cartera | 4 h de corrección |
| Sprint 4 | Percentiles P10/P90 sin decimales | 4 h de corrección |

## Lectura del histórico

- **Capacidad nominal:** 5 personas × 8 h × 10 días = 400 h por sprint de dos semanas; el Sprint 0 fue una semana (200 h).
- **Utilización:** 1.163 h registradas sobre 1.752 h disponibles (66 %). El resto se cubrió con ceremonias, revisiones, soporte y colchón.
- **Puntos:** 284 puntos comprometidos; 277 entregados. En el Sprint 4, 58 de los 65 puntos ya están cerrados y quedan 7 en UAT/review.
- **Versiones:** v0.1 en el Sprint 1, v0.2/v0.3 en el Sprint 2, v0.4/v0.5 en el Sprint 3 y v0.6.0 en el Sprint 4.

## Enlaces

- Proyecto: [BUD · Bud Jet](https://jaxsullivan7.atlassian.net/jira/software/projects/BUD/summary)
- Board: [BUD board](https://jaxsullivan7.atlassian.net/jira/software/projects/BUD/boards/6)
- Filtros: [Andrea](https://jaxsullivan7.atlassian.net/issues/?filter=10038) · [Pablo](https://jaxsullivan7.atlassian.net/issues/?filter=10039) · [Marta](https://jaxsullivan7.atlassian.net/issues/?filter=10040) · [Diego](https://jaxsullivan7.atlassian.net/issues/?filter=10041) · [Lucía](https://jaxsullivan7.atlassian.net/issues/?filter=10042)
- [Ausencias](https://jaxsullivan7.atlassian.net/issues/?filter=10043) · [Impedimentos y bugs](https://jaxsullivan7.atlassian.net/issues/?filter=10044)
