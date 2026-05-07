"""
chart_generator.py
Python Analysis Engine — ChartGenerator component
SD-4 Step 7: createPieChart(df: DataFrame) : void

Called by ChartGenerator.java via ProcessBuilder:
    python3 chart_generator.py <input_csv> <output_png>

Input CSV format:
    category,amount
    Food,150.0
    Transport,80.0
    Entertainment,200.0
"""

import sys
import os
import pandas as pd
import matplotlib
matplotlib.use('Agg')  # non-interactive backend for subprocess use
import matplotlib.pyplot as plt

def create_pie_chart(csv_path: str, output_path: str) -> None:
    """
    SD-4: createPieChart(df: DataFrame) : void
    Reads expense data from CSV and generates a pie chart PNG.
    """
    # Read data from CSV passed by Java
    df = pd.read_csv(csv_path)

    if df.empty:
        print("No data to chart.")
        sys.exit(1)

    # ── Chart styling ─────────────────────────────────────────────────────────
    fig, ax = plt.subplots(figsize=(7, 5), facecolor='#1A1A1A')
    ax.set_facecolor('#1A1A1A')

    colors = [
        '#C9A84C',  # gold
        '#378ADD',  # blue
        '#4CAF50',  # green
        '#E07840',  # orange
        '#9C6FD6',  # purple
        '#E05555',  # red
        '#40C4D6',  # cyan
    ]

    wedges, texts, autotexts = ax.pie(
        df['amount'],
        labels=df['category'],
        autopct='%1.1f%%',
        colors=colors[:len(df)],
        startangle=140,
        pctdistance=0.82,
        wedgeprops=dict(width=0.6, edgecolor='#0D0D0D', linewidth=2)
    )

    # Style labels
    for text in texts:
        text.set_color('#CCCCCC')
        text.set_fontsize(11)
        text.set_fontfamily('DejaVu Sans')

    for autotext in autotexts:
        autotext.set_color('#EEEEEE')
        autotext.set_fontsize(10)
        autotext.set_fontweight('bold')

    # Title
    ax.set_title(
        'Spending by Category',
        color='#C9A84C',
        fontsize=14,
        fontweight='bold',
        pad=20
    )

    # Legend
    ax.legend(
        wedges,
        [f"{row['category']}  {row['amount']:.1f} EGP" for _, row in df.iterrows()],
        loc='lower center',
        bbox_to_anchor=(0.5, -0.18),
        ncol=2,
        fontsize=9,
        facecolor='#252525',
        edgecolor='#333333',
        labelcolor='#CCCCCC'
    )

    plt.tight_layout()
    plt.savefig(output_path, dpi=150, bbox_inches='tight',
                facecolor='#1A1A1A', edgecolor='none')
    plt.close()

    print(f"Chart saved to {output_path}")


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print("Usage: python3 chart_generator.py <input.csv> <output.png>")
        sys.exit(1)

    csv_input  = sys.argv[1]
    png_output = sys.argv[2]

    if not os.path.exists(csv_input):
        print(f"Input file not found: {csv_input}")
        sys.exit(1)

    create_pie_chart(csv_input, png_output)
