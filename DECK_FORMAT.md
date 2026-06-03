# Formato de mazo KokoroMed — Especificación JSON

## Estructura general

```json
{
  "deck": { ... },
  "cards": [ ... ]
}
```

---

## Objeto `deck`

| Campo         | Tipo   | Obligatorio | Descripción |
|---------------|--------|-------------|-------------|
| `name`        | string | ✅           | Nombre del mazo. Máximo recomendado: 60 caracteres. |
| `description` | string | ❌           | Descripción breve del contenido. Default: `""` |
| `color`       | string | ❌           | Color en hexadecimal. Default: `"#0d9488"` |
| `icon`        | string | ❌           | Nombre del icono. Default: `"stack"` |

### Colores disponibles (recomendados)

```
#0d9488  #059669  #16a34a  #4ade80
#2563eb  #0891b2  #6366f1
#7c3aed  #a21caf  #db2777
#e11d48  #ea580c  #d97706
```

Puedes usar cualquier color hexadecimal válido (`#RRGGBB`).

### Iconos disponibles

```
heart · pill · droplet · brain · stack · layers · stethoscope · trophy
```

---

## Objeto `cards` (array)

Cada elemento del array representa una tarjeta de tipo **múltiple opción** con exactamente **4 opciones**.

| Campo         | Tipo     | Obligatorio | Descripción |
|---------------|----------|-------------|-------------|
| `question`    | string   | ✅           | Enunciado de la pregunta. |
| `options`     | string[] | ✅           | Array de exactamente **4** opciones de respuesta. |
| `correct`     | integer  | ✅           | Índice (0–3) de la opción correcta. |
| `explanation` | string   | ❌           | Explicación que se muestra tras responder. Recomendado. |
| `tags`        | string[] | ❌           | Etiquetas para categorizar la tarjeta. Default: `[]` |
| `difficulty`  | string   | ❌           | `"easy"`, `"medium"` o `"hard"`. Default: `"medium"` |

---

## Ejemplo completo

```json
{
  "deck": {
    "name": "Farmacología — Antibióticos",
    "description": "Mecanismos de acción y clasificación de antimicrobianos.",
    "color": "#2563eb",
    "icon": "pill"
  },
  "cards": [
    {
      "question": "¿Cuál es el mecanismo de acción de los β-lactámicos?",
      "options": [
        "Inhiben la síntesis proteica (30S)",
        "Inhiben la síntesis de la pared celular",
        "Inhiben la ADN girasa",
        "Alteran la membrana citoplasmática"
      ],
      "correct": 1,
      "explanation": "Los β-lactámicos inhiben las transpeptidasas (PBP), bloqueando la síntesis del peptidoglicano.",
      "tags": ["mecanismo", "beta-lactámicos"],
      "difficulty": "medium"
    },
    {
      "question": "¿Qué antibiótico es de elección para SARM?",
      "options": [
        "Amoxicilina",
        "Vancomicina",
        "Azitromicina",
        "Ciprofloxacino"
      ],
      "correct": 1,
      "explanation": "La vancomicina (glicopéptido) es el antibiótico de referencia frente a Staphylococcus aureus resistente a meticilina.",
      "tags": ["uso clínico", "SARM"],
      "difficulty": "hard"
    }
  ]
}
```

---

## Reglas de validación

- `options` debe contener **exactamente 4** elementos.
- `correct` debe ser un entero entre **0 y 3** (inclusive).
- `difficulty` acepta solo: `"easy"`, `"medium"`, `"hard"`.
- `icon` acepta solo los valores listados arriba; cualquier otro valor usa `"stack"` por defecto.
- Campos desconocidos son **ignorados** por el parser (safe to include extra metadata).

---

## Prompt para IA

```
Genera un mazo de flashcards en formato JSON para la app KokoroMed, siguiendo
estrictamente esta estructura:

{
  "deck": {
    "name": "<nombre del mazo>",
    "description": "<descripción breve>",
    "color": "<color hex, elige uno de: #0d9488 #2563eb #7c3aed #ea580c #e11d48 #16a34a #d97706 #0891b2>",
    "icon": "<uno de: heart pill droplet brain stack layers stethoscope trophy>"
  },
  "cards": [
    {
      "question": "<pregunta clara y directa>",
      "options": ["<opción A>", "<opción B>", "<opción C>", "<opción D>"],
      "correct": <índice 0-3 de la opción correcta>,
      "explanation": "<explicación concisa de por qué esa es la respuesta correcta>",
      "tags": ["<tag1>", "<tag2>"],
      "difficulty": "<easy | medium | hard>"
    }
  ]
}

REGLAS OBLIGATORIAS:
- Exactamente 4 opciones por tarjeta.
- "correct" es el índice (0, 1, 2 o 3) de la opción correcta dentro del array.
- Solo responde con el JSON, sin texto adicional antes ni después.
- El JSON debe ser válido y parseable.
- Usa comillas dobles, nunca simples.
- La explicación debe ser educativa y mencionar por qué las otras opciones son incorrectas si es relevante.

Tema: [ESCRIBE AQUÍ EL TEMA]
Número de tarjetas: [ESCRIBE AQUÍ CUÁNTAS]
Dificultad predominante: [easy / medium / hard]
```
