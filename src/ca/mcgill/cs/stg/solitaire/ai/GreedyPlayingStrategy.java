/*******************************************************************************
 * Solitaire
 *
 * Copyright (C) 2016 by Martin P. Robillard
 *
 * See: https://github.com/prmr/Solitaire
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package ca.mcgill.cs.stg.solitaire.ai;

import java.util.ArrayList;
import java.util.List;

import ca.mcgill.cs.stg.solitaire.cards.Card;
import ca.mcgill.cs.stg.solitaire.model.GameModel.StackIndex;
import ca.mcgill.cs.stg.solitaire.model.GameModel.SuitStackIndex;
import ca.mcgill.cs.stg.solitaire.model.GameModelView;
import ca.mcgill.cs.stg.solitaire.model.Move;

/**
 * Makes the first possible move in this order: 
 * 0. Discarding if the discard pile is empty
 * 1. Moving a card from the discard pile to a suit stack
 * 2. Moving a card from the discard pile to a working stack
 * 3. Moving a card from a working stack to a suit stack, in order
 * of working stacks.
 * 4. Moving from a working stack to another, if this either reveals
 * a fresh card or frees up a spot for a kind.
 * 5. If no moves are possible, discards.
 */
public class GreedyPlayingStrategy implements PlayingStrategy
{
	// CSOFF:
	@Override 
	public Move computeNextMove(GameModelView pModel)
	{
		if( pModel.isEmptyDiscardPile() && !pModel.isEmptyDeck() )
		{
			return pModel.getDiscardMove();
		}
		else
		{
			ArrayList<Move> moves = new ArrayList<>();
			moves.addAll(movesFromWorkingStacksRevealsCard(pModel));
			if( moves.size() > 0 )
			{
				return moves.get(0);
			}
			moves.addAll(movesFromDiscardPileToSuitStack(pModel));
			if( moves.size() > 0 )
			{
				return moves.get(0);
			}
			moves.addAll(movesFromDiscardPileToWorkingStacks(pModel));
			if( moves.size() > 0 )
			{
				return moves.get(0);
			}
			moves.addAll(movesFromWorkingStacksToSuitStacks(pModel));
			if( moves.size() > 0 )
			{
				return moves.get(0);
			}
			
			if( !pModel.isEmptyDeck() )
			{
				return pModel.getDiscardMove();
			}
			else
			{
				return pModel.getNullMove();
			}
		}
	} // CSON:
	
	private List<Move> movesFromDiscardPileToSuitStack(GameModelView pModel)
	{
		ArrayList<Move> moves = new ArrayList<>();
		if( !pModel.isEmptyDiscardPile())
		{
			for(SuitStackIndex index : SuitStackIndex.values())
			{
				if( pModel.isLegalMove(pModel.peekDiscardPile(), index))
				{
					moves.add(pModel.getCardMove(pModel.peekDiscardPile(), index));
					if( pModel.isEmptySuitStack(index))
					{
						break; // we take the first possible blank space
					}
				}
		}}
		return moves;
	}
	
	private List<Move> movesFromDiscardPileToWorkingStacks(GameModelView pModel)
	{
		ArrayList<Move> moves = new ArrayList<>();
		if( !pModel.isEmptyDiscardPile() )
		{
			for(StackIndex index : StackIndex.values())
			{
				if( pModel.isLegalMove(pModel.peekDiscardPile(), index))
				{
					moves.add(pModel.getCardMove(pModel.peekDiscardPile(), index));
				}
			}
		}
		return moves;
	}
	
	private List<Move> movesFromWorkingStacksToSuitStacks(GameModelView pModel)
	{
		ArrayList<Move> moves = new ArrayList<>();
		for(StackIndex index : StackIndex.values())
		{
			Card[] stack = pModel.getStack(index);
			if( stack.length > 0 )
			{
				Card card = stack[stack.length-1];
				for(SuitStackIndex index2 : SuitStackIndex.values())
				{
					if( pModel.isLegalMove(card, index2))
					{
						moves.add(pModel.getCardMove(card, index2));
						if( pModel.isEmptySuitStack(index2))
						{
							break; // we take the first possible blank space
						}
					}
				}
			}	
		}
		return moves;
	}
	
	private List<Move> movesFromWorkingStacksRevealsCard(GameModelView pModel)
	{
		ArrayList<Move> moves = new ArrayList<>();
		for(StackIndex index : StackIndex.values())
		{
			Card[] stack = pModel.getStack(index);
			for(int i = 0; i < stack.length; i++ )
			{
				if( pModel.isVisibleInWorkingStack(stack[i]) && i > 0 && !pModel.isVisibleInWorkingStack(stack[i-1]) )
				{
					for( StackIndex index2 : StackIndex.values() )
					{
						if( pModel.isLegalMove(stack[i], index2))
						{
							moves.add(pModel.getCardMove(stack[i], index2));
						}
					}
				}
				else if( pModel.isVisibleInWorkingStack(stack[i]) && i == 0 )
				{
					for( StackIndex index2 : StackIndex.values() )
					{
						// we don't want to just move a card around
						if( pModel.isLegalMove(stack[i], index2) && pModel.getStack(index2).length > 0) 
						{
							moves.add(pModel.getCardMove(stack[i], index2));
						}
					}
				}
			}
		}
		
		return moves;
	}
}
