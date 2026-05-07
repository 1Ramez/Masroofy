"""
@file chart_generator.py
@brief Generates a spending-by-category pie chart from a CSV file.

This script is invoked by the Java controller:
    python3 chart_generator.py <input_csv> <output_png>

The input CSV must contain the columns: category,amount.
"""

import os
import sys

import matplotlib
import matplotlib.pyplot as plt
import pandas as pd

matplotlib.use("Agg")


def create_pie_chart(csv_path: str, output_path: str) -> None:
    """
    Reads expense data from a CSV file and generates a pie chart PNG.

    @param csv_path Path to the input CSV file.
    @param output_path Path to the output PNG file.
    """
    df = pd.read_csv(csv_path)

    if df.empty:
        print("No data to chart.")
        sys.exit(1)

    fig, ax = plt.subplots(figsize=(7, 5), facecolor="#1A1A1A")
    ax.set_facecolor("#1A1A1A")

    colors = [
        "#C9A84C",
        "#378ADD",
        "#4CAF50",
        "#E07840",
        "#9C6FD6",
        "#E05555",
        "#40C4D6",
    ]

    wedges, texts, autotexts = ax.pie(
        df["amount"],
        labels=df["category"],
        autopct="%1.1f%%",
        colors=colors[: len(df)],
        startangle=140,
        pctdistance=0.82,
        wedgeprops=dict(width=0.6, edgecolor="#0D0D0D", linewidth=2),
    )

    for text in texts:
        text.set_color("#CCCCCC")
        text.set_fontsize(11)
        text.set_fontfamily("DejaVu Sans")

    for autotext in autotexts:
        autotext.set_color("#EEEEEE")
        autotext.set_fontsize(10)
        autotext.set_fontweight("bold")

    ax.set_title(
        "Spending by Category",
        color="#C9A84C",
        fontsize=14,
        fontweight="bold",
        pad=20,
    )

    ax.legend(
        wedges,
        [f"{row['category']}  {row['amount']:.1f} EGP" for _, row in df.iterrows()],
        loc="lower center",
        bbox_to_anchor=(0.5, -0.18),
        ncol=2,
        fontsize=9,
        facecolor="#252525",
        edgecolor="#333333",
        labelcolor="#CCCCCC",
    )

    plt.tight_layout()
    plt.savefig(
        output_path,
        dpi=150,
        bbox_inches="tight",
        facecolor="#1A1A1A",
        edgecolor="none",
    )
    plt.close()

    print(f"Chart saved to {output_path}")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python3 chart_generator.py <input.csv> <output.png>")
        sys.exit(1)

    csv_input = sys.argv[1]
    png_output = sys.argv[2]

    if not os.path.exists(csv_input):
        print(f"Input file not found: {csv_input}")
        sys.exit(1)

    create_pie_chart(csv_input, png_output)

